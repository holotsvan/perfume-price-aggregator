package com.aggregator.service;

import com.aggregator.dto.PerfumePriceDto;
import com.aggregator.entity.Perfume;
import com.aggregator.entity.PerfumePrice;
import com.aggregator.entity.PriceHistory;
import com.aggregator.entity.Store;
import com.aggregator.parser.ParserFactory;
import com.aggregator.parser.PriceParser;
import com.aggregator.repository.PerfumePriceRepository;
import com.aggregator.repository.PerfumeRepository;
import com.aggregator.repository.StoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PriceAggregationService {

    private final ParserFactory parserFactory;
    private final PerfumeRepository perfumeRepository;
    private final StoreRepository storeRepository;
    private final PerfumePriceRepository perfumePriceRepository;

    @Autowired
    public PriceAggregationService(ParserFactory parserFactory, PerfumeRepository perfumeRepository, StoreRepository storeRepository, PerfumePriceRepository perfumePriceRepository) {
        this.parserFactory = parserFactory;
        this.perfumeRepository = perfumeRepository;
        this.storeRepository = storeRepository;
        this.perfumePriceRepository = perfumePriceRepository;
    }

    @Transactional
    public List<PerfumePriceDto> fetchVariants(Long perfumeId, String productUrl) {
        Optional<PriceParser> parserOpt = parserFactory.getParser(productUrl);
        if (parserOpt.isEmpty()) {
            throw new IllegalArgumentException("No suitable parser found for URL: " + productUrl);
        }
        
        // This just scrapes the page and returns combinations, doesn't save to DB yet
        List<PerfumePriceDto> variants = parserOpt.get().parseVariants(productUrl);
        if (variants == null || variants.isEmpty()) {
            throw new IllegalArgumentException("Could not extract any variant options from this URL.");
        }
        
        variants.forEach(v -> v.setPerfumeId(perfumeId));
        return variants;
    }

    @Transactional
    public PerfumePriceDto trackVariant(PerfumePriceDto parsedData) {
        Perfume perfume = perfumeRepository.findById(parsedData.getPerfumeId())
                .orElseThrow(() -> new IllegalArgumentException("Perfume not found"));

        if (parsedData.getPrice() == null) {
            throw new IllegalArgumentException("Cannot track variant without a price.");
        }

        // Find or create store
        Store store = storeRepository.findByName(parsedData.getStoreName())
                .orElseGet(() -> {
                    Store newStore = new Store();
                    newStore.setName(parsedData.getStoreName());
                    newStore.setParserType(parsedData.getStoreName() + "Parser"); // Simple guess based on store name
                    return storeRepository.save(newStore);
                });

        // Try to find exact variant tracked
        Optional<PerfumePrice> existingPriceOpt = perfumePriceRepository.findAll().stream()
                .filter(p -> p.getPerfume().getId().equals(perfume.getId()) 
                          && p.getStore().getId().equals(store.getId())
                          && (p.getVariantName() != null && p.getVariantName().equals(parsedData.getVariantName())))
                .findFirst();

        PerfumePrice perfumePrice;
        if (existingPriceOpt.isPresent()) {
            perfumePrice = existingPriceOpt.get();
            // Record history if price changed
            if (perfumePrice.getPrice() != null && perfumePrice.getPrice().compareTo(parsedData.getPrice()) != 0) {
                PriceHistory history = new PriceHistory();
                history.setPrice(perfumePrice.getPrice());
                history.setCurrency(perfumePrice.getCurrency());
                history.setTimestamp(LocalDateTime.now());
                history.setPerfumePrice(perfumePrice);
                perfumePrice.getHistory().add(history);
            }
            // Update to new values
            perfumePrice.setPrice(parsedData.getPrice());
            perfumePrice.setCurrency(parsedData.getCurrency());
            perfumePrice.setVolume(parsedData.getVolume());
        } else {
            perfumePrice = new PerfumePrice();
            perfumePrice.setPerfume(perfume);
            perfumePrice.setStore(store);
            perfumePrice.setVolume(parsedData.getVolume());
            perfumePrice.setVariantName(parsedData.getVariantName());
            perfumePrice.setProductUrl(parsedData.getProductUrl());
            perfumePrice.setPrice(parsedData.getPrice());
            perfumePrice.setCurrency(parsedData.getCurrency());
            
            // Record initial history
            PriceHistory history = new PriceHistory();
            history.setPrice(parsedData.getPrice());
            history.setCurrency(parsedData.getCurrency());
            history.setTimestamp(LocalDateTime.now());
            history.setPerfumePrice(perfumePrice);
            perfumePrice.getHistory().add(history);
        }

        PerfumePrice saved = perfumePriceRepository.save(perfumePrice);
        
        parsedData.setId(saved.getId());
        return parsedData;
    }

    // Run every 6 hours
    @Scheduled(fixedRate = 21600000)
    @Transactional
    public void updateAllPrices() {
        log.info("Starting scheduled price update for all products...");
        List<PerfumePrice> allPrices = perfumePriceRepository.findAll();
        
        int successCount = 0;
        int errorCount = 0;
        
        for (PerfumePrice p : allPrices) {
            if ("Manual".equals(p.getStore().getParserType())) continue; // Skip manual entries
            
            try {
                // To fetch an updated price for this tracked variant, we refetch all from the URL
                // and match the same variantName
                Optional<PriceParser> parserOpt = parserFactory.getParser(p.getProductUrl());
                if (parserOpt.isPresent()) {
                    List<PerfumePriceDto> latestOpts = parserOpt.get().parseVariants(p.getProductUrl());
                    
                    Optional<PerfumePriceDto> match = latestOpts.stream()
                        .filter(v -> v.getVariantName() != null && v.getVariantName().equals(p.getVariantName()))
                        .findFirst();
                        
                    if(match.isPresent()) {
                        match.get().setPerfumeId(p.getPerfume().getId());
                        trackVariant(match.get());
                        successCount++;
                    } else {
                        throw new RuntimeException("Variant " + p.getVariantName() + " no longer directly found on page.");
                    }
                }
                
                // Slight delay to avoid hammering servers too aggressively
                Thread.sleep(1000);
            } catch (Exception e) {
                errorCount++;
                log.error("Failed to update price for ID {}: {}", p.getId(), e.getMessage());
            }
        }
        
        log.info("Finished price update. Success: {}, Errors: {}", successCount, errorCount);
    }
    
    @Transactional
    public PerfumePriceDto saveManualPrice(PerfumePriceDto dto) {
        Perfume perfume = perfumeRepository.findById(dto.getPerfumeId())
                .orElseThrow(() -> new IllegalArgumentException("Perfume not found"));
        
        Store store = storeRepository.findByName(dto.getStoreName())
                .orElseGet(() -> {
                    Store newStore = new Store();
                    newStore.setName(dto.getStoreName());
                    newStore.setParserType("Manual");
                    return storeRepository.save(newStore);
                });
                
        Optional<PerfumePrice> existingPriceOpt = perfumePriceRepository.findAll().stream()
                .filter(p -> p.getPerfume().getId().equals(perfume.getId()) 
                          && p.getStore().getId().equals(store.getId())
                          && p.getVariantName() != null && p.getVariantName().equals(dto.getVariantName()))
                .findFirst();

        PerfumePrice perfumePrice;
        if (existingPriceOpt.isPresent()) {
            perfumePrice = existingPriceOpt.get();
            if (perfumePrice.getPrice() != null && perfumePrice.getPrice().compareTo(dto.getPrice()) != 0) {
                PriceHistory history = new PriceHistory();
                history.setPrice(perfumePrice.getPrice());
                history.setCurrency(perfumePrice.getCurrency());
                history.setTimestamp(LocalDateTime.now());
                history.setPerfumePrice(perfumePrice);
                perfumePrice.getHistory().add(history);
            }
            perfumePrice.setPrice(dto.getPrice());
            perfumePrice.setCurrency(dto.getCurrency());
            if (dto.getProductUrl() != null && !dto.getProductUrl().isEmpty()) {
                perfumePrice.setProductUrl(dto.getProductUrl());
            }
        } else {
            perfumePrice = new PerfumePrice();
            perfumePrice.setPerfume(perfume);
            perfumePrice.setStore(store);
            perfumePrice.setVolume(dto.getVolume());
            perfumePrice.setVariantName(dto.getVariantName());
            perfumePrice.setProductUrl(dto.getProductUrl());
            perfumePrice.setPrice(dto.getPrice());
            perfumePrice.setCurrency(dto.getCurrency());
            
            PriceHistory history = new PriceHistory();
            history.setPrice(dto.getPrice());
            history.setCurrency(dto.getCurrency());
            history.setTimestamp(LocalDateTime.now());
            history.setPerfumePrice(perfumePrice);
            perfumePrice.getHistory().add(history);
        }

        PerfumePrice saved = perfumePriceRepository.save(perfumePrice);
        dto.setId(saved.getId());
        return dto;
    }

    @Transactional
    public void deletePrice(Long id) {
        perfumePriceRepository.deleteById(id);
    }
}

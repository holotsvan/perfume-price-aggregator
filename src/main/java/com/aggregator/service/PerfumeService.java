package com.aggregator.service;

import com.aggregator.currency.CurrencyService;
import com.aggregator.dto.PerfumeDto;
import com.aggregator.dto.PerfumePriceDto;
import com.aggregator.dto.PriceHistoryDto;
import com.aggregator.entity.Perfume;
import com.aggregator.entity.PerfumePrice;
import com.aggregator.repository.PerfumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PerfumeService {

    private final PerfumeRepository perfumeRepository;
    private final CurrencyService currencyService;

    @Autowired
    public PerfumeService(PerfumeRepository perfumeRepository, CurrencyService currencyService) {
        this.perfumeRepository = perfumeRepository;
        this.currencyService = currencyService;
    }

    @Transactional(readOnly = true)
    public List<PerfumeDto> getAllPerfumes() {
        return perfumeRepository.findAll().stream()
                .map(p -> mapToDto(p, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PerfumeDto getPerfumeDetails(Long id) {
        return perfumeRepository.findById(id)
                .map(p -> mapToDto(p, true))
                .orElse(null);
    }

    @Transactional
    public PerfumeDto createPerfume(PerfumeDto dto) {
        Perfume perfume = new Perfume();
        perfume.setName(dto.getName());
        perfume.setBrand(dto.getBrand());
        perfume.setGender(dto.getGender());
        
        Perfume saved = perfumeRepository.save(perfume);
        return mapToDto(saved, false);
    }

    @Transactional
    public void deletePerfume(Long id) {
        perfumeRepository.deleteById(id);
    }

    private PerfumeDto mapToDto(Perfume perfume, boolean includePrices) {
        PerfumeDto dto = new PerfumeDto();
        dto.setId(perfume.getId());
        dto.setName(perfume.getName());
        dto.setBrand(perfume.getBrand());
        dto.setGender(perfume.getGender());
        dto.setCreatedAt(perfume.getCreatedAt());

        if (includePrices && perfume.getPrices() != null) {
            List<PerfumePriceDto> priceDtos = perfume.getPrices().stream()
                    .map(this::mapPriceToDto)
                    .collect(Collectors.toList());
            dto.setPrices(priceDtos);
        }

        return dto;
    }

    private PerfumePriceDto mapPriceToDto(PerfumePrice price) {
        PerfumePriceDto dto = new PerfumePriceDto();
        dto.setId(price.getId());
        dto.setPrice(price.getPrice());
        dto.setCurrency(price.getCurrency());
        dto.setVolume(price.getVolume());
        dto.setProductUrl(price.getProductUrl());
        dto.setCreatedAt(price.getCreatedAt());
        
        dto.setPerfumeId(price.getPerfume().getId());
        dto.setPerfumeName(price.getPerfume().getName());
        
        dto.setStoreId(price.getStore().getId());
        dto.setStoreName(price.getStore().getName());
        
        // Calculate dynamic values
        if (price.getPrice() != null && price.getCurrency() != null) {
            BigDecimal usdPrice = currencyService.convertToUsd(price.getPrice(), price.getCurrency());
            dto.setConvertedPriceUsd(usdPrice);
            
            if (usdPrice != null && price.getVolume() != null && price.getVolume() > 0) {
                BigDecimal mlPrice = usdPrice.divide(new BigDecimal(price.getVolume()), 4, RoundingMode.HALF_UP);
                dto.setPricePerMlUsd(mlPrice);
            }
        }
        
        if (price.getHistory() != null) {
            java.util.List<PriceHistoryDto> hList = price.getHistory().stream().map(h -> {
                PriceHistoryDto hd = new PriceHistoryDto();
                hd.setId(h.getId());
                hd.setPrice(h.getPrice());
                hd.setCurrency(h.getCurrency());
                hd.setTimestamp(h.getTimestamp());
                return hd;
            }).collect(Collectors.toList());
            dto.setHistory(hList);
        }
        
        return dto;
    }
}

package com.aggregator.service;

import com.aggregator.dto.StoreDto;
import com.aggregator.entity.Store;
import com.aggregator.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StoreService {

    private final StoreRepository storeRepository;

    @Autowired
    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Transactional(readOnly = true)
    public List<StoreDto> getAllStores() {
        return storeRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public StoreDto createStore(StoreDto dto) {
        Store store = new Store();
        store.setName(dto.getName());
        store.setWebsite(dto.getWebsite());
        store.setParserType(dto.getParserType());
        
        Store saved = storeRepository.save(store);
        return mapToDto(saved);
    }
    
    private StoreDto mapToDto(Store store) {
        StoreDto dto = new StoreDto();
        dto.setId(store.getId());
        dto.setName(store.getName());
        dto.setWebsite(store.getWebsite());
        dto.setParserType(store.getParserType());
        return dto;
    }
}

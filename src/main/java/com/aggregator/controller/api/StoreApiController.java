package com.aggregator.controller.api;

import com.aggregator.dto.StoreDto;
import com.aggregator.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreApiController {

    private final StoreService storeService;

    @Autowired
    public StoreApiController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping
    public ResponseEntity<List<StoreDto>> getAllStores() {
        return ResponseEntity.ok(storeService.getAllStores());
    }

    @PostMapping
    public ResponseEntity<StoreDto> createStore(@RequestBody StoreDto dto) {
        return ResponseEntity.ok(storeService.createStore(dto));
    }
}

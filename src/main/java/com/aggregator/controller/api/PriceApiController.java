package com.aggregator.controller.api;

import com.aggregator.dto.PerfumePriceDto;
import com.aggregator.service.PriceAggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/prices")
public class PriceApiController {

    private final PriceAggregationService aggregationService;
    private final com.aggregator.service.ManualHtmlService manualHtmlService;

    @Autowired
    public PriceApiController(PriceAggregationService aggregationService, com.aggregator.service.ManualHtmlService manualHtmlService) {
        this.aggregationService = aggregationService;
        this.manualHtmlService = manualHtmlService;
    }

    @PostMapping("/parse")
    public ResponseEntity<?> fetchVariants(@RequestBody Map<String, Object> payload) {
        try {
            Long perfumeId = Long.valueOf(payload.get("perfumeId").toString());
            String url = (String) payload.get("url");

            List<PerfumePriceDto> variants = aggregationService.fetchVariants(perfumeId, url);
            return ResponseEntity.ok(variants);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching variants: ", e);
            return ResponseEntity.internalServerError().body("An unexpected error occurred while parsing the URL.");
        }
    }

    @PostMapping("/parse-manual")
    public ResponseEntity<?> parseManualHtml(@RequestBody Map<String, Object> payload) {
        try {
            String url = (String) payload.get("url");
            String html = (String) payload.get("html");

            List<PerfumePriceDto> variants = manualHtmlService.parseManualHtml(url, html);
            return ResponseEntity.ok(variants);
        } catch (Exception e) {
            log.error("Error parsing manual HTML: ", e);
            return ResponseEntity.internalServerError().body("An unexpected error occurred while parsing the provided HTML.");
        }
    }
    
    @PostMapping("/track")
    public ResponseEntity<?> trackVariant(@RequestBody PerfumePriceDto variantData) {
        try {
            PerfumePriceDto saved = aggregationService.trackVariant(variantData);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error tracking variant: ", e);
            return ResponseEntity.internalServerError().body("An unexpected error occurred while tracking the variant.");
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePrice(@PathVariable Long id) {
        try {
            aggregationService.deletePrice(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting price: ", e);
            return ResponseEntity.internalServerError().body("An unexpected error occurred while deleting the price.");
        }
    }
}

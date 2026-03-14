package com.aggregator.dto;

import com.aggregator.currency.Currency;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PerfumePriceDto {
    private Long id;
    private BigDecimal price;
    private Currency currency;
    private Integer volume;
    private String variantName;
    private String productUrl;
    private LocalDateTime createdAt;
    
    private Long perfumeId;
    private String perfumeName;
    
    private Long storeId;
    private String storeName;
    
    private BigDecimal convertedPriceUsd;
    private BigDecimal pricePerMlUsd;
    
    private java.util.List<PriceHistoryDto> history = new java.util.ArrayList<>();
}

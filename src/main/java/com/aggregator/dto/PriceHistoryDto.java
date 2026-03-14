package com.aggregator.dto;

import com.aggregator.currency.Currency;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PriceHistoryDto {
    private Long id;
    private BigDecimal price;
    private Currency currency;
    private LocalDateTime timestamp;
}

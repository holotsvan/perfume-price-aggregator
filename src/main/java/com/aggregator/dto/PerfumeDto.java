package com.aggregator.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PerfumeDto {
    private Long id;
    private String name;
    private String brand;
    private String gender;
    private LocalDateTime createdAt;
    private List<PerfumePriceDto> prices;
}

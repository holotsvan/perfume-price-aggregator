package com.aggregator.entity;

import com.aggregator.currency.Currency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "perfume_price")
@Getter
@Setter
public class PerfumePrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    private Currency currency;
    
    private Integer volume; // in ml
    
    @Column(name = "variant_name")
    private String variantName;
    
    @Column(name = "product_url", length = 2048)
    private String productUrl;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfume_id")
    private Perfume perfume;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;
    
    @OneToMany(mappedBy = "perfumePrice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceHistory> history = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

package com.aggregator.currency;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

@Service
public class CurrencyService {
    
    // Mock exchange rates relative to USD base
    private static final Map<Currency, BigDecimal> USD_RATES = new EnumMap<>(Currency.class);
    
    static {
        USD_RATES.put(Currency.USD, BigDecimal.ONE);
        USD_RATES.put(Currency.EUR, new BigDecimal("0.92"));    // 1 USD = 0.92 EUR
        USD_RATES.put(Currency.UAH, new BigDecimal("39.50"));   // 1 USD = 39.50 UAH
        USD_RATES.put(Currency.PLN, new BigDecimal("3.98"));    // 1 USD = 3.98 PLN
        USD_RATES.put(Currency.UNKNOWN, BigDecimal.ONE); // Fallback
    }

    public BigDecimal convertToUsd(BigDecimal price, Currency currency) {
        if (price == null || currency == null) {
            return null;
        }
        if (currency == Currency.USD) {
            return price;
        }
        
        BigDecimal rate = USD_RATES.get(currency);
        if (rate == null || rate.compareTo(BigDecimal.ZERO) == 0) {
            return price; // Fallback
        }
        
        // price / rate = USD value
        return price.divide(rate, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal convertFromUsd(BigDecimal priceUsd, Currency targetCurrency) {
        if (priceUsd == null || targetCurrency == null) {
            return null;
        }
        if (targetCurrency == Currency.USD) {
            return priceUsd;
        }
        
        BigDecimal rate = USD_RATES.get(targetCurrency);
        if (rate == null) {
            return priceUsd; // Fallback
        }
        
        // USD value * rate = target currency value
        return priceUsd.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}

package com.aggregator.currency;

public class CurrencyResolver {
    
    public static Currency resolve(String text) {
        if (text == null) {
            return Currency.UNKNOWN;
        }
        
        String lowerText = text.toLowerCase();
        
        if (lowerText.contains("$") || lowerText.contains("usd")) {
            return Currency.USD;
        } else if (lowerText.contains("€") || lowerText.contains("eur")) {
            return Currency.EUR;
        } else if (lowerText.contains("₴") || lowerText.contains("грн") || lowerText.contains("uah")) {
            return Currency.UAH;
        } else if (lowerText.contains("zł") || lowerText.contains("pln")) {
            return Currency.PLN;
        }
        
        return Currency.UNKNOWN;
    }
}

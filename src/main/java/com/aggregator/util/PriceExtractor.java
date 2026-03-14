package com.aggregator.util;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PriceExtractor {
    
    // Matches digits, optional spaces, and decimal separators (, or .)
    private static final Pattern PRICE_PATTERN = Pattern.compile("(\\d+[\\d\\s]*[.,]?\\d*)");

    public static BigDecimal extract(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        Matcher matcher = PRICE_PATTERN.matcher(text);
        if (matcher.find()) {
            String match = matcher.group(1);
            // Remove spaces
            match = match.replaceAll("\\s+", "");
            // Replace comma with dot for BigDecimal parsing
            match = match.replace(',', '.');
            try {
                return new BigDecimal(match);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public static Integer extractVolume(String text) {
        if (text == null) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?i)(\\d+)\\s*(ml|мл|oz)").matcher(text);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch(Exception e){}
        }
        // Fallback if it's just a number
        try {
            String digits = text.replaceAll("\\D", "");
            if (!digits.isEmpty() && digits.length() <= 4) {
                 return Integer.parseInt(digits);
            }
        } catch(Exception e){}
        return null;
    }
}

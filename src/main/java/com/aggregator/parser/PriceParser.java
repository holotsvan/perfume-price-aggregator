package com.aggregator.parser;

import com.aggregator.dto.PerfumePriceDto;
import org.jsoup.nodes.Document;
import java.util.List;

public interface PriceParser {
    String getStoreName();
    boolean supports(String url);
    List<PerfumePriceDto> parseVariants(String productUrl);
    List<PerfumePriceDto> parseDocument(Document doc, String productUrl);
}

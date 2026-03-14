package com.aggregator.service;

import com.aggregator.dto.PerfumePriceDto;
import com.aggregator.parser.ParserFactory;
import com.aggregator.parser.PriceParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManualHtmlService {

    private final ParserFactory parserFactory;

    /**
     * Parses product variants from provided HTML content and URL.
     * 
     * @param url The product URL to identify the correct parser.
     * @param html The raw HTML content pasted by the user.
     * @return List of parsed product variants.
     */
    public List<PerfumePriceDto> parseManualHtml(String url, String html) {
        if (url == null || html == null || html.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            PriceParser parser = parserFactory.getParser(url)
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported store URL: " + url));
            Document doc = Jsoup.parse(html, url);
            return parser.parseDocument(doc, url);
        } catch (Exception e) {
            log.error("Error parsing manual HTML for URL {}: {}", url, e.getMessage());
            return Collections.emptyList();
        }
    }
}

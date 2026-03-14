package com.aggregator.parser;

import com.aggregator.currency.Currency;
import com.aggregator.currency.CurrencyResolver;
import com.aggregator.dto.PerfumePriceDto;
import com.aggregator.util.PriceExtractor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class BrocardParser implements PriceParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getStoreName() {
        return "Brocard";
    }

    @Override
    public boolean supports(String url) {
        return url != null && url.contains("brocard.ua");
    }

    @Override
    public List<PerfumePriceDto> parseVariants(String productUrl) {
        try {
            Document doc = Jsoup.connect(productUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .header("Accept-Language", "uk,en-US;q=0.9,en;q=0.8")
                    .timeout(15000)
                    .ignoreHttpErrors(true)
                    .get();

            return parseDocument(doc, productUrl);
        } catch (Exception e) {
            log.error("Error connecting to Brocard URL {}: {}", productUrl, e.getMessage());
            return null;
        }
    }

    @Override
    public List<PerfumePriceDto> parseDocument(Document doc, String productUrl) {
        List<PerfumePriceDto> variants = new ArrayList<>();
        String mainName = "";
        Element nameEl = doc.selectFirst("h1.product-title, h1, .product-item__name");
        if (nameEl != null) {
            mainName = nameEl.text().trim();
        } else {
            mainName = doc.title().replace(" - BROCARD", "").trim();
        }

        // 1. Try JSON-LD
        try {
            Elements scripts = doc.select("script[type=application/ld+json]");
            for (Element script : scripts) {
                String html = script.html();
                if (html.contains("\"Product\"")) {
                    JsonNode node = objectMapper.readTree(html);
                    if (node.isArray()) {
                        for (JsonNode item : node) {
                            extractFromJsonLd(item, variants, mainName, productUrl);
                        }
                    } else {
                        extractFromJsonLd(node, variants, mainName, productUrl);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing Brocard JSON-LD: {}", e.getMessage());
        }

        if (!variants.isEmpty()) return variants;

        // 2. DOM Fallback
        Element priceEl = doc.selectFirst(".price-format .price, .product-price__big, .price .wysiwyg, .product-item__price");
        if (priceEl != null) {
            PerfumePriceDto dto = new PerfumePriceDto();
            dto.setProductUrl(productUrl);
            dto.setStoreName(getStoreName());
            dto.setPerfumeName(mainName);

            String priceRaw = priceEl.text();
            dto.setPrice(PriceExtractor.extract(priceRaw));

            String currencyText = priceRaw;
            Element currencyEl = doc.selectFirst(".currency-symbol, .price-currency");
            if (currencyEl != null) currencyText = currencyEl.text();
            dto.setCurrency(CurrencyResolver.resolve(currencyText));

            // Get the ACTIVE Selected Volume
            Element activeVolumeEl = doc.selectFirst(".swatch-wrapper .swatch-label, .options-holder .selected .swatch-label, .swatch-attribute-selected-option");
            if (activeVolumeEl != null) {
                String volText = activeVolumeEl.text().trim();
                dto.setVariantName(volText);
                dto.setVolume(PriceExtractor.extractVolume(volText));
            } else {
                dto.setVariantName("Standard");
                dto.setVolume(PriceExtractor.extractVolume(mainName));
            }

            if (dto.getPrice() != null) {
                variants.add(dto);
            }
        }

        return variants;
    }

    private void extractFromJsonLd(JsonNode node, List<PerfumePriceDto> variants, String mainName, String productUrl) {
        if (node.has("@type") && node.get("@type").asText().equals("Product") && node.has("offers")) {
            JsonNode offersNode = node.get("offers");
            
            if (offersNode.has("offers") && offersNode.get("offers").isArray()) {
                for (JsonNode offer : offersNode.get("offers")) {
                    addVariantFromOffer(offer, variants, mainName, productUrl);
                }
            } else if (offersNode.isArray()) {
                for (JsonNode offer : offersNode) {
                    addVariantFromOffer(offer, variants, mainName, productUrl);
                }
            } else {
                addVariantFromOffer(offersNode, variants, mainName, productUrl);
            }
        }
    }

    private void addVariantFromOffer(JsonNode offer, List<PerfumePriceDto> variants, String mainName, String productUrl) {
        try {
            if (offer.has("price") && !offer.get("price").isNull()) {
                PerfumePriceDto dto = new PerfumePriceDto();
                dto.setProductUrl(productUrl);
                dto.setStoreName(getStoreName());
                dto.setPerfumeName(mainName);
                
                dto.setPrice(PriceExtractor.extract(offer.get("price").asText()));
                if (offer.has("priceCurrency")) {
                    dto.setCurrency(CurrencyResolver.resolve(offer.get("priceCurrency").asText()));
                } else {
                    dto.setCurrency(Currency.UAH);
                }

                String vName = offer.has("name") ? offer.get("name").asText() : "Standard";
                dto.setVariantName(vName);
                dto.setVolume(PriceExtractor.extractVolume(vName));
                
                if (dto.getVolume() == null) {
                    dto.setVolume(PriceExtractor.extractVolume(mainName));
                }

                if (dto.getPrice() != null) {
                    variants.add(dto);
                }
            }
        } catch (Exception e) {}
    }
}

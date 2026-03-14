package com.aggregator.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ParserFactory {

    private final List<PriceParser> parsers;

    @Autowired
    public ParserFactory(List<PriceParser> parsers) {
        this.parsers = parsers;
    }

    public Optional<PriceParser> getParser(String url) {
        return parsers.stream()
                .filter(p -> p.supports(url))
                .findFirst();
    }
}

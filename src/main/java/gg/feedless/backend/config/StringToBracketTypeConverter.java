package gg.feedless.backend.config;

import gg.feedless.backend.stats.BracketType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToBracketTypeConverter implements Converter<String, BracketType> {
    @Override
    public BracketType convert(String source) {
        return BracketType.valueOf(source.toUpperCase());
    }
}

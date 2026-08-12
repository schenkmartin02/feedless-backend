package gg.feedless.backend.config;

import gg.feedless.backend.stats.MatchQueueFilter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToMatchQueueFilterConverter implements Converter<String, MatchQueueFilter> {
    @Override
    public MatchQueueFilter convert(String source) {
        return MatchQueueFilter.valueOf(source.toUpperCase());
    }
}

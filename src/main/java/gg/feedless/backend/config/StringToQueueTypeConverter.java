package gg.feedless.backend.config;

import gg.feedless.backend.stats.QueueType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToQueueTypeConverter implements Converter<String, QueueType> {
    @Override
    public QueueType convert(String source) {
        return QueueType.valueOf(source.toUpperCase());
    }
}

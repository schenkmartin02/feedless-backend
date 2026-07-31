package gg.feedless.backend.config;

import gg.feedless.backend.stats.RegionType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToRegionTypeConverter implements Converter<String, RegionType> {
    @Override
    public RegionType convert(String source) {
        return RegionType.valueOf(source.toUpperCase());
    }
}

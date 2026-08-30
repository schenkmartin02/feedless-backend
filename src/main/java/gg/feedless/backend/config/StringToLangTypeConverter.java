package gg.feedless.backend.config;

import gg.feedless.backend.changelog.LangType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToLangTypeConverter implements Converter<String, LangType> {
    @Override
    public LangType convert(String source){
        return LangType.valueOf(source.toUpperCase());
    }
}

package gg.feedless.backend.config;

import gg.feedless.backend.stats.RoleType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToRoleTypeConverter implements Converter<String, RoleType> {
    @Override
    public RoleType convert(String source) {
        return RoleType.fromParam(source);
    }
}

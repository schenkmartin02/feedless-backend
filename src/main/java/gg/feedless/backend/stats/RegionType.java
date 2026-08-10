package gg.feedless.backend.stats;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public enum RegionType {
    EUNE("EUN1"),
    EUW("EUW1");

    private final String platform;

    private static final Map<String, RegionType> BY_REGION =
            Arrays.stream(values())
                    .collect(Collectors.toMap(RegionType::getPlatform, c -> c));

    RegionType(String platform) {
        this.platform = platform;
    }

    public static Optional<RegionType> fromSymbol(String platform){
        return Optional.ofNullable(BY_REGION.get(platform));
    }

    public String getPlatform() {
        return platform;
    }
}

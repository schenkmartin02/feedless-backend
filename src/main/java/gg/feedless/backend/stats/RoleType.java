package gg.feedless.backend.stats;

public enum RoleType {
    TOP("Top"),
    JUNGLE("Jungle"),
    MIDDLE("Mid"),
    BOTTOM("Bot"),
    UTILITY("Support");

    private final String role;

    RoleType(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public static RoleType fromTeamPosition(String teamPosition) {
        return switch (teamPosition) {
            case "TOP" -> RoleType.TOP;
            case "JUNGLE" -> RoleType.JUNGLE;
            case "MIDDLE" -> RoleType.MIDDLE;
            case "BOTTOM" -> RoleType.BOTTOM;
            case "UTILITY" -> RoleType.UTILITY;
            default -> RoleType.MIDDLE;
        };
    }

    public static RoleType fromParam(String param) {
        for (RoleType role: RoleType.values()) {
            if (param.equalsIgnoreCase(role.getRole())) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + param);
    }
}

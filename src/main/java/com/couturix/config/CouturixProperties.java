package com.couturix.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "couturix")
public record CouturixProperties(
        Jwt jwt,
        Storage storage,
        Stock stock,
        Notifications notifications,
        Bootstrap bootstrap
) {
    public record Jwt(String secret, long accessTokenMinutes, long refreshTokenDays) {}
    public record Storage(String directory, String publicUrlPrefix) {}
    public record Stock(boolean allowNegative) {}
    public record Notifications(String fromEmail, Whatsapp whatsapp) {
        public record Whatsapp(boolean enabled, String apiUrl, String phoneNumberId, String accessToken) {}
    }
    public record Bootstrap(String adminEmail, String adminPassword, String adminNom, String adminPrenom) {}
}

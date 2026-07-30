package gg.feedless.backend.riot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RiotApiConfig {

    @Bean
    public RestClient europeRestClient(@Value("${riot.api-key}") String apiKey, @Value("${riot.rate-limit.per-10-seconds}") int per10Seconds, @Value("${riot.rate-limit.per-10-minutes}") int per10Minutes) {
        return createRestClientWithLimiter("https://europe.api.riotgames.com", apiKey, per10Seconds, per10Minutes);
    }

    @Bean
    public RestClient eun1RestClient(@Value("${riot.api-key}") String apiKey, @Value("${riot.rate-limit.per-10-seconds}") int per10Seconds, @Value("${riot.rate-limit.per-10-minutes}") int per10Minutes) {
        return createRestClientWithLimiter("https://eun1.api.riotgames.com", apiKey, per10Seconds, per10Minutes);
    }

    @Bean
    public RestClient euw1RestClient(@Value("${riot.api-key}") String apiKey, @Value("${riot.rate-limit.per-10-seconds}") int per10Seconds, @Value("${riot.rate-limit.per-10-minutes}") int per10Minutes) {
        return createRestClientWithLimiter("https://euw1.api.riotgames.com", apiKey, per10Seconds, per10Minutes);
    }

    private RestClient createRestClientWithLimiter(String url, String apiKey,int per10Seconds, int per10Minutes) {
        RiotApiRateLimiter limiter = new RiotApiRateLimiter(per10Seconds, per10Minutes);
        return RestClient.builder().baseUrl(url).requestInterceptor(limiter).defaultHeader("X-Riot-Token", apiKey).build();
    }

}

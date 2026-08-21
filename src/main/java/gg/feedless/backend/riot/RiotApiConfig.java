package gg.feedless.backend.riot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RiotApiConfig {

    @Bean
    public RestClient europeRestClient(@Value("${riot.api-key}") String apiKey, @Value("${riot.rate-limit.per-10-seconds}") int per10Seconds, @Value("${riot.rate-limit.per-10-minutes}") int per10Minutes, RiotApiMetrics riotApiMetrics) {
        return createRestClientWithLimiter("https://europe.api.riotgames.com", apiKey, per10Seconds, per10Minutes, "europe", riotApiMetrics);
    }

    @Bean
    public RestClient eun1RestClient(@Value("${riot.api-key}") String apiKey, @Value("${riot.rate-limit.per-10-seconds}") int per10Seconds, @Value("${riot.rate-limit.per-10-minutes}") int per10Minutes, RiotApiMetrics riotApiMetrics) {
        return createRestClientWithLimiter("https://eun1.api.riotgames.com", apiKey, per10Seconds, per10Minutes, "eun1", riotApiMetrics);
    }

    @Bean
    public RestClient euw1RestClient(@Value("${riot.api-key}") String apiKey, @Value("${riot.rate-limit.per-10-seconds}") int per10Seconds, @Value("${riot.rate-limit.per-10-minutes}") int per10Minutes, RiotApiMetrics riotApiMetrics) {
        return createRestClientWithLimiter("https://euw1.api.riotgames.com", apiKey, per10Seconds, per10Minutes, "euw1", riotApiMetrics);
    }

    private RestClient createRestClientWithLimiter(String url, String apiKey,int per10Seconds, int per10Minutes, String bucketName, RiotApiMetrics riotApiMetrics) {
        RiotApiRateLimiter limiter = new RiotApiRateLimiter(per10Seconds, per10Minutes, bucketName, riotApiMetrics);
        riotApiMetrics.register(bucketName, per10Seconds);
        return RestClient.builder().baseUrl(url).requestInterceptor(limiter).defaultHeader("X-Riot-Token", apiKey).build();
    }

}

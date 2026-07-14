package gg.feedless.backend.riot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RiotApiConfig {

    @Bean
    public RestClient europeRestClient(@Value("${riot.api-key}") String apiKey) {
        return RestClient.builder().baseUrl("https://europe.api.riotgames.com").defaultHeader("X-Riot-Token", apiKey).build();
    }

}

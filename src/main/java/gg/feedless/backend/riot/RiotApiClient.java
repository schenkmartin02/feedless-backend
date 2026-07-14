package gg.feedless.backend.riot;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
public class RiotApiClient {
    private final RestClient riotApi = RestClient.builder().baseUrl("https://europe.api.riotgames.com").defaultHeader("X-Riot-Token", System.getenv("RIOT_KEY")).build();

    public Optional<AccountDto> getAccountByNameAndTag(String gameName, String tagLine) {
        AccountDto result;
        try {
            result = riotApi.get().uri("/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}", gameName, tagLine).retrieve().body(AccountDto.class);
        } catch (HttpClientErrorException error) {
            if (error.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return Optional.empty();
            } else {
                throw error;
            }
        }
        return Optional.ofNullable(result);
    }
}

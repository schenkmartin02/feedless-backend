package gg.feedless.backend.riot;

import gg.feedless.backend.riot.dto.AccountDto;
import gg.feedless.backend.riot.dto.match.MatchDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Component
public class RiotApiClient {
    private final RestClient riotApi;

    public RiotApiClient(@Qualifier("europeRestClient") RestClient riotApi) {
        this.riotApi = riotApi;
    }


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

    public List<String> getMatchListByPuuidDefault(String puuid) {

        List<String> result;
        try {
            ParameterizedTypeReference<List<String>> typeRef = new ParameterizedTypeReference<>() {};
            result = riotApi.get().uri("/lol/match/v5/matches/by-puuid/{puuid}/ids?start=0&count=20", puuid).retrieve().body(typeRef);
        } catch (HttpClientErrorException error) {
            throw error;
        }

        return result;
    }

    public Optional<MatchDto> getMatchByMatchId(String matchId) {
        MatchDto result;
        try {
            result = riotApi.get().uri("/lol/match/v5/matches/{matchId}", matchId).retrieve().body(MatchDto.class);
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

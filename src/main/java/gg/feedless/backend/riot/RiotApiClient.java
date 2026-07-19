package gg.feedless.backend.riot;

import gg.feedless.backend.riot.dto.account.AccountDto;
import gg.feedless.backend.riot.dto.league.LeagueEntryDto;
import gg.feedless.backend.riot.dto.match.MatchDto;
import gg.feedless.backend.riot.dto.summoner.SummonerDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class RiotApiClient {
    private final RestClient europeApi;
    private final RestClient eun1Api;

    public RiotApiClient(@Qualifier("europeRestClient") RestClient europeApi, @Qualifier("eun1RestClient") RestClient eun1Api) {
        this.europeApi = europeApi;
        this.eun1Api = eun1Api;
    }

    public Optional<AccountDto> getAccountByNameAndTag(String gameName, String tagLine) {
        AccountDto result;
        try {
            result = europeApi.get().uri("/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}", gameName, tagLine).retrieve().body(AccountDto.class);
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
        ParameterizedTypeReference<List<String>> typeRef = new ParameterizedTypeReference<>() {};
        return europeApi.get().uri("/lol/match/v5/matches/by-puuid/{puuid}/ids?start=0&count=10", puuid).retrieve().body(typeRef);
    }

    public MatchDto getMatchByMatchId(String matchId) {
        return europeApi.get().uri("/lol/match/v5/matches/{matchId}", matchId).retrieve().body(MatchDto.class);
    }

    public SummonerDto getSummonerByPuuid(String puuid) {
        return eun1Api.get().uri("/lol/summoner/v4/summoners/by-puuid/{puuid}", puuid).retrieve().body(SummonerDto.class);
    }

    public Set<LeagueEntryDto> getLeagueByPuuid(String puuid) {
        ParameterizedTypeReference<Set<LeagueEntryDto>> typeRef = new ParameterizedTypeReference<>() {};
        return eun1Api.get().uri("/lol/league/v4/entries/by-puuid/{puuid}", puuid).retrieve().body(typeRef);
    }
}

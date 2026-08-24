package gg.feedless.backend.riot;

import gg.feedless.backend.riot.dto.account.AccountDto;
import gg.feedless.backend.riot.dto.league.LeagueEntryDto;
import gg.feedless.backend.riot.dto.league.LeagueListDto;
import gg.feedless.backend.riot.dto.match.MatchDto;
import gg.feedless.backend.riot.dto.spectator.CurrentGameDto;
import gg.feedless.backend.riot.dto.summoner.SummonerDto;
import gg.feedless.backend.riot.dto.timeline.TimelineDto;
import gg.feedless.backend.stats.QueueType;
import gg.feedless.backend.stats.RegionType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.*;

@Component
public class RiotApiClient {
    private final RestClient europeApi;
    private final RestClient eun1Api;
    private final RestClient euw1Api;

    public RiotApiClient(@Qualifier("europeRestClient") RestClient europeApi, @Qualifier("eun1RestClient") RestClient eun1Api, @Qualifier("euw1RestClient") RestClient euw1Api) {
        this.europeApi = europeApi;
        this.eun1Api = eun1Api;
        this.euw1Api = euw1Api;
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

    public Optional<AccountDto> getAccountByPuuid(String puuid) {
        AccountDto result;
        try {
            result = europeApi.get().uri("/riot/account/v1/accounts/by-puuid/{puuid}", puuid).retrieve().body(AccountDto.class);
        } catch (HttpClientErrorException error) {
            if (error.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return Optional.empty();
            } else {
                throw error;
            }
        }
        return Optional.ofNullable(result);
    }

    public List<String> getMatchListByPuuid(String puuid, int count, long startTime) {
        ParameterizedTypeReference<List<String>> typeRef = new ParameterizedTypeReference<>() {};
        return europeApi.get().uri("/lol/match/v5/matches/by-puuid/{puuid}/ids?start=0&count={count}&startTime={startTime}", puuid, count, startTime).retrieve().body(typeRef);
    }

    public List<String> getFirstMatchIdByPuuid(String puuid) {
        ParameterizedTypeReference<List<String>> typeRef = new ParameterizedTypeReference<>() {};
        return europeApi.get().uri("/lol/match/v5/matches/by-puuid/{puuid}/ids?start=0&count=1", puuid).retrieve().body(typeRef);
    }

    public MatchDto getMatchByMatchId(String matchId) {
        return europeApi.get().uri("/lol/match/v5/matches/{matchId}", matchId).retrieve().body(MatchDto.class);
    }

    public Optional<SummonerDto> getSummonerByPuuid(String region, String puuid) {
        try {
            RestClient client = getRestClientByRegion(region);
            if (client != null) {
                return Optional.ofNullable(client.get().uri("/lol/summoner/v4/summoners/by-puuid/{puuid}", puuid).retrieve().body(SummonerDto.class));
            }
        } catch (HttpClientErrorException error) {
            if (error.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return Optional.empty();
            } else {
                throw error;
            }
        }
        return Optional.empty();
    }

    public Set<LeagueEntryDto> getLeagueByPuuid(String puuid, String region) {
        ParameterizedTypeReference<Set<LeagueEntryDto>> typeRef = new ParameterizedTypeReference<>() {};
        RestClient client = getRestClientByRegion(region);
        if (client != null) {
            return client.get().uri("/lol/league/v4/entries/by-puuid/{puuid}", puuid).retrieve().body(typeRef);
        }
        return new HashSet<>();
    }

    public Optional<LeagueListDto> getLeaderboardLeagues(QueueType queue, String region, ApexTier tier) {
        RestClient client = getRestClientByRegion(region);
        if (queue.getLeagueQueue() == null) {
            return Optional.empty();
        }
        if (client != null) {
            return Optional.ofNullable(client.get().uri("/lol/league/v4/{tier}/by-queue/{queue}", tier.getLeagues(), queue.getLeagueQueue()).retrieve().body(LeagueListDto.class));
        }
        return Optional.empty();
    }

    public Optional<CurrentGameDto> getActiveGameByPuuid(String puuid, String region){
        RestClient client = getRestClientByRegion(region);
        try {
            if (client != null) {
                return Optional.ofNullable(client.get().uri("/lol/spectator/v5/active-games/by-summoner/{puuid}", puuid).retrieve().body(CurrentGameDto.class));
            }
        } catch (HttpClientErrorException error) {
            if (error.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return Optional.empty();
            } else {
                throw error;
            }
        }
        return Optional.empty();
    }

    public Optional<TimelineDto> getMatchTimeLineByMatchId(String matchId){
        try {
                return Optional.ofNullable(europeApi.get().uri("/lol/match/v5/matches/{matchId}/timeline", matchId).retrieve().body(TimelineDto.class));
        } catch (HttpClientErrorException error) {
            if (error.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return Optional.empty();
            } else {
                throw error;
            }
        }
    }

    private RestClient getRestClientByRegion(String region) {
        if (Objects.equals(region, RegionType.EUNE.getPlatform())) {
            return eun1Api;
        } else if (Objects.equals(region, RegionType.EUW.getPlatform())) {
            return euw1Api;
        }
        return null;
    }
}

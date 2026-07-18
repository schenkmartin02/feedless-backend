package gg.feedless.backend;

import gg.feedless.backend.riot.dto.account.AccountDto;
import gg.feedless.backend.riot.RiotApiClient;
import gg.feedless.backend.riot.dto.match.MatchDto;
import gg.feedless.backend.riot.dto.summoner.SummonerDto;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class BackendApplication implements CommandLineRunner {

    private final RiotApiClient riotApiClient;

    public BackendApplication(RiotApiClient riotApiClient) {
        this.riotApiClient = riotApiClient;
    }

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Override
    public void run(String @NonNull ... args) {
        Optional<AccountDto> account = riotApiClient.getAccountByNameAndTag("MartinOMG", "HUN");
        List<String> matchList = List.of();
        SummonerDto summoner = null;
        if (account.isPresent()) {
            matchList = riotApiClient.getMatchListByPuuidDefault(account.get().puuid());
            summoner = riotApiClient.getSummonerByPuuid(account.get().puuid());
        }
        List<MatchDto> match = new ArrayList<>();
        if (!matchList.isEmpty()) {
            for (String s : matchList) {
                match.add(riotApiClient.getMatchByMatchId(s));
            }
        }
        System.out.println(match.size());

        for (MatchDto matchDto : match) {
            System.out.println(matchDto.info().queueId());
        }

        System.out.println(summoner);
    }

}

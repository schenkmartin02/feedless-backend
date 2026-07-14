package gg.feedless.backend;

import gg.feedless.backend.riot.dto.AccountDto;
import gg.feedless.backend.riot.RiotApiClient;
import gg.feedless.backend.riot.dto.match.MatchDto;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
        if (account.isPresent()) {
            matchList = riotApiClient.getMatchListByPuuidDefault(account.get().puuid());
        }
        Optional<MatchDto> match = Optional.empty();
        if (!matchList.isEmpty()) {
            match = riotApiClient.getMatchByMatchId(matchList.getFirst());
        }
        System.out.println(match);
    }

}

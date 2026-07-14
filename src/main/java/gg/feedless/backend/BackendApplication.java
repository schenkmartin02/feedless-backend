package gg.feedless.backend;

import gg.feedless.backend.riot.AccountDto;
import gg.feedless.backend.riot.RiotApiClient;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
        Optional<AccountDto> result = riotApiClient.getAccountByNameAndTag("MartinOMG", "HUN");
        System.out.println(result);
    }

}

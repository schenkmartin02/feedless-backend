package gg.feedless.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Bean
    public ExecutorService matchFetchExecutor() {
        return Executors.newFixedThreadPool(24);
    }

    @Bean
    public ExecutorService rankFetchExecutor() {
        return Executors.newFixedThreadPool(20);
    }

    @Bean
    public ExecutorService crawlExecutor(@Value("${crawler.workers}") int workers) {
        return Executors.newFixedThreadPool(workers);
    }

}

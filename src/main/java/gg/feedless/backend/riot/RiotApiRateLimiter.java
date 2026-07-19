package gg.feedless.backend.riot;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class RiotApiRateLimiter implements ClientHttpRequestInterceptor {

    private final Bucket bucket;

    private static final int MAX_RETRIES = 5;
    private static final long DEFAULT_WAIT_SECONDS = 2;

    public RiotApiRateLimiter(int per10Second, int per10Minutes) {
        Bandwidth secondLimit = Bandwidth.builder().capacity(per10Second)
                .refillIntervally(per10Second, Duration.ofSeconds(10)).build();
        Bandwidth minuteLimit = Bandwidth.builder().capacity(per10Minutes)
                .refillIntervally(per10Minutes, Duration.ofMinutes(10)).build();
        this.bucket = Bucket.builder().addLimit(secondLimit).addLimit(minuteLimit).build();
    }

    @Override
    @NullMarked public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        try {
            bucket.asBlocking().consume(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting", e);
        }
        int currentTry = 0;
        //TODO: attempt-ciklusba szervezni
        ClientHttpResponse response = execution.execute(request, body);
        if (!response.getStatusCode().equals(HttpStatus.TOO_MANY_REQUESTS)) {
            return response;
        }
        while (currentTry < MAX_RETRIES) {
            List<String> getRetry = response.getHeaders().get("Retry-After");

            try {
                if (getRetry != null) {
                    Thread.sleep(Long.parseLong(getRetry.getFirst()) * 1000);
                } else {
                    Thread.sleep(DEFAULT_WAIT_SECONDS * 1000);
                }
                bucket.asBlocking().consume(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting", e);
            }
            response.close();
            response = execution.execute(request, body);
            if (!response.getStatusCode().equals(HttpStatus.TOO_MANY_REQUESTS)) {
                return response;
            }
            currentTry++;
        }
        response.close();
        throw new IOException("Riot API still returned 429 after " + MAX_RETRIES + " attempts, giving up. The API key quota may be exhausted by another process. URI: " + request.getURI());
    }
}

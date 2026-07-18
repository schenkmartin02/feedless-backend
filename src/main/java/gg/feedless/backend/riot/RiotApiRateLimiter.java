package gg.feedless.backend.riot;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
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

    public RiotApiRateLimiter(int per10Second, int per10Minutes) {
        Bandwidth secondLimit = Bandwidth.builder().capacity(per10Second)
                .refillIntervally(per10Second, Duration.ofSeconds(10)).build();
        Bandwidth minuteLimit = Bandwidth.builder().capacity(per10Minutes)
                .refillIntervally(per10Minutes, Duration.ofMinutes(10)).build();
        this.bucket = Bucket.builder().addLimit(secondLimit).addLimit(minuteLimit).build();
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        try {
            bucket.asBlocking().consume(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting", e);
        }
        int maxTry = 5;
        int currentTry = 0;
        long waitTime = 2;
        //TODO: attempt-ciklusba szervezni
        ClientHttpResponse response = execution.execute(request, body);
        if (!response.getStatusCode().equals(HttpStatus.TOO_MANY_REQUESTS)) {
            return response;
        }
        while (currentTry < maxTry) {
            List<String> getRetry = response.getHeaders().get("Retry-After");
            if (getRetry != null) {
                waitTime = Long.parseLong(getRetry.getFirst());
            }
            try {
                Thread.sleep(waitTime * 1000);
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
        throw new IOException("Riot API still returned 429 after " + maxTry + " attempts, giving up. The API key quota may be exhausted by another process. URI: " + request.getURI());
    }
}

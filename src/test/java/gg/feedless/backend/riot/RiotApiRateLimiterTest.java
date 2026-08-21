package gg.feedless.backend.riot;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import
        org.springframework.mock.http.client.MockClientHttpRequest;
import
        org.springframework.mock.http.client.MockClientHttpResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RiotApiRateLimiterTest {

    @Test
    void returnsResponseWhenRequestSucceeds() throws IOException {
        RiotApiRateLimiter limiter = new RiotApiRateLimiter(100,
                100, "test", new RiotApiMetrics());
        MockClientHttpRequest request = new
                MockClientHttpRequest();
        MockClientHttpResponse okResponse = new
                MockClientHttpResponse(new byte[0], HttpStatus.OK);

        ClientHttpResponse response = limiter.intercept(request,
                new byte[0], (req, body) -> okResponse);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void retriesUntilSuccess() throws IOException {
        RiotApiRateLimiter limiter = new RiotApiRateLimiter(100,
                100, "test", new RiotApiMetrics());
        MockClientHttpRequest request = new
                MockClientHttpRequest();
        MockClientHttpResponse okResponse = new
                MockClientHttpResponse(new byte[0], HttpStatus.OK);
        MockClientHttpResponse toManyRequestResponse = new
                MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS);
        toManyRequestResponse.getHeaders().add("Retry-After", "0");

        AtomicInteger calls = new AtomicInteger(0);
        ClientHttpResponse response = limiter.intercept(request,
                new byte[0], (req, body) -> calls.incrementAndGet() < 3 ? toManyRequestResponse : okResponse);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, calls.get());
    }

    @Test
    void throwsWhenRetriesExhausted() {
        RiotApiRateLimiter limiter = new RiotApiRateLimiter(100,
                100, "test", new RiotApiMetrics());
        MockClientHttpRequest request = new
                MockClientHttpRequest();
        MockClientHttpResponse toManyRequestResponse = new
                MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS);
        toManyRequestResponse.getHeaders().add("Retry-After", "0");

        AtomicInteger calls = new AtomicInteger(0);
        assertThrows(IOException.class, () -> limiter.intercept(request, new byte[0], (req, body) -> {
            calls.incrementAndGet();
            return toManyRequestResponse;
        }));
        assertEquals(6, calls.get());
    }

}
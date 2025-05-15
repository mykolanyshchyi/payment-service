package com.payment.service.provider;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class MockPaymentProviderB extends AbstractPaymentProvider {

    @Autowired
    public MockPaymentProviderB(@Value("${providers.a.url:http://localhost:8081}") String baseUrl,
                                WebClient.Builder builder,
                                @Qualifier("providerB-cb") CircuitBreaker circuitBreaker,
                                @Qualifier("providerB-rl") RateLimiter rateLimiter,
                                @Qualifier("providerB-retry") Retry retry) {

        super(baseUrl, builder, "PROVIDER_B", circuitBreaker, rateLimiter, retry);
    }
}

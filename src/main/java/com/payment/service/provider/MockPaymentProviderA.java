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
public class MockPaymentProviderA extends AbstractPaymentProvider {

    @Autowired
    public MockPaymentProviderA(@Value("${providers.a.url:http://localhost:8081}") String baseUrl,
                                WebClient.Builder builder,
                                @Qualifier("providerA-cb") CircuitBreaker circuitBreaker,
                                @Qualifier("providerA-rl") RateLimiter rateLimiter,
                                @Qualifier("providerA-retry") Retry retry) {
        super(baseUrl, builder, "PROVIDER_A", circuitBreaker, rateLimiter, retry);
    }
}

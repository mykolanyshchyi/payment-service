package com.payment.configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ResilienceConfiguration {

    @Bean("providerA-cb")
    public CircuitBreaker providerACircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("PROVIDER_A");
    }

    @Bean("providerA-retry")
    public Retry providerARetry(RetryRegistry registry) {
        return registry.retry("PROVIDER_A");
    }

    @Bean("providerA-rl")
    public RateLimiter rateLimiterA(RateLimiterRegistry registry) {
        return registry.rateLimiter("PROVIDER_A");
    }

    @Bean("providerB-cb")
    public CircuitBreaker providerBCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("PROVIDER_B");
    }

    @Bean("providerB-retry")
    public Retry providerBRetry(RetryRegistry registry) {
        return registry.retry("PROVIDER_B");
    }

    @Bean("providerB-rl")
    public RateLimiter rateLimiterB(RateLimiterRegistry registry) {
        return registry.rateLimiter("PROVIDER_B");
    }
}

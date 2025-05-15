package com.payment.service.provider;

import com.payment.model.dto.PaymentProviderResponse;
import com.payment.model.dto.PaymentRequest;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
public abstract class AbstractPaymentProvider implements PaymentProvider {

    private final WebClient client;
    private final String providerId;
    private final CircuitBreaker circuitBreaker;
    private final RateLimiter rateLimiter;
    private final Retry retry;

    public AbstractPaymentProvider(String baseUrl, WebClient.Builder builder, String providerId,
                                   CircuitBreaker circuitBreaker, RateLimiter rateLimiter, Retry retry) {
        this.client = builder.baseUrl(baseUrl).build();
        this.providerId = providerId;
        this.circuitBreaker = circuitBreaker;
        this.rateLimiter = rateLimiter;
        this.retry = retry;
    }

    @Override
    public PaymentProviderResponse pay(PaymentRequest request) {
        Supplier<PaymentProviderResponse> coreCall = createCall(request);
        Supplier<PaymentProviderResponse> guardedCall = decorateCall(coreCall);

        try {
            return guardedCall.get();
        } catch (WebClientResponseException e) {
            log.error("Payment failed. Provider: {}, Status code: {}. Message: {}", providerId, e.getStatusCode(), e.getMessage());
            return new PaymentProviderResponse(null, "failed", e.getMessage(), providerId);
        }
    }

    private Supplier<PaymentProviderResponse> createCall(PaymentRequest request) {
        return () -> client.post()
                .uri("/payments")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentProviderResponse.class)
                .timeout(Duration.ofSeconds(2))
                .block();
    }

    private Supplier<PaymentProviderResponse> decorateCall(Supplier<PaymentProviderResponse> coreCall) {
        return Decorators.ofSupplier(coreCall)
                .withCircuitBreaker(circuitBreaker)
                .withRateLimiter(rateLimiter)
                .withRetry(retry)
                .decorate();
    }
}

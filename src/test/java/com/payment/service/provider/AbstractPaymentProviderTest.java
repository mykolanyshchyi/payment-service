package com.payment.service.provider;

import com.payment.model.dto.PaymentProviderResponse;
import com.payment.model.dto.PaymentRequest;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Currency;

import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbstractPaymentProviderTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> headersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private CircuitBreaker circuitBreaker;

    @Mock
    private RateLimiter rateLimiter;

    @Mock
    private Retry retry;

    private TestPaymentProvider provider;

    @BeforeEach
    void setUp() {

        WebClient.Builder webClientBuilder = mock(WebClient.Builder.class);
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/payments")).thenReturn(requestBodySpec);

//        when(requestBodySpec.bodyValue(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        provider = new TestPaymentProvider("http://fake-url", webClientBuilder, "providerX", circuitBreaker, rateLimiter, retry);
    }

    @Test
    void shouldReturnSuccessfulResponse() {
        PaymentProviderResponse expected = new PaymentProviderResponse("txn1", "success", null, "providerX");

        when(responseSpec.bodyToMono(PaymentProviderResponse.class)).thenReturn(Mono.just(expected));

        PaymentRequest request = new PaymentRequest("4242424242424242", TEN, USD);
        PaymentProviderResponse response = provider.pay(request);

        assertThat(response).isEqualTo(expected);
    }

    @Test
    void shouldHandleWebClientResponseExceptionGracefully() {
        WebClientResponseException exception = WebClientResponseException.create(
                400, "Bad Request", null, null, null
        );

        when(responseSpec.bodyToMono(PaymentProviderResponse.class)).thenReturn(Mono.error(exception));

        PaymentRequest request = new PaymentRequest("5555555555554444", TEN, USD);
        PaymentProviderResponse response = provider.pay(request);

        assertThat(response.getStatus()).isEqualTo("failed");
        assertThat(response.getMessage()).isEqualTo("400 Bad Request");
        assertThat(response.getProviderId()).isEqualTo("providerX");
    }

    private static class TestPaymentProvider extends AbstractPaymentProvider {
        public TestPaymentProvider(String baseUrl, WebClient.Builder builder, String providerId,
                                   CircuitBreaker circuitBreaker, RateLimiter rateLimiter, Retry retry) {
            super(baseUrl, builder, providerId, circuitBreaker, rateLimiter, retry);
        }
    }
}

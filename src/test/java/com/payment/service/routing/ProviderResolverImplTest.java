package com.payment.service.routing;

import com.payment.model.PaymentRoutingRule;
import com.payment.model.dto.PaymentRequest;
import com.payment.service.provider.PaymentProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ProviderResolverImplTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");

    @Mock
    private PaymentProvider providerA;

    @Mock
    private PaymentProvider providerB;

    @Mock
    private PaymentProvider defaultProvider;

    private ProviderResolverImpl resolver;

    @BeforeEach
    void setUp() {
        List<PaymentRoutingRule> rules = List.of(
                new PaymentRoutingRule(req -> req.bin().startsWith("4"), providerA),
                new PaymentRoutingRule(req -> req.currency().equals(EUR), providerB),
                new PaymentRoutingRule(req -> req.amount().compareTo(new BigDecimal("1000")) > 0, providerB)
        );

        this.resolver = new ProviderResolverImpl(rules, defaultProvider);
    }

    @Test
    void shouldReturnProviderA_WhenBinStartsWith4() {
        PaymentRequest request = new PaymentRequest("4123456789012345", BigDecimal.valueOf(500), USD);
        PaymentProvider resolved = resolver.resolve(request);
        assertThat(resolved).isEqualTo(providerA);
    }

    @Test
    void shouldReturnProviderB_WhenCurrencyIsEUR() {
        PaymentRequest request = new PaymentRequest("5123456789012345", BigDecimal.valueOf(200), EUR);
        PaymentProvider resolved = resolver.resolve(request);
        assertThat(resolved).isEqualTo(providerB);
    }

    @Test
    void shouldReturnDefaultProvider_WhenNoRulesMatch() {
        PaymentRequest request = new PaymentRequest("5123456789012345", BigDecimal.valueOf(200), USD);
        PaymentProvider resolved = resolver.resolve(request);
        assertThat(resolved).isEqualTo(defaultProvider);
    }

    @Test
    void shouldReturnFirstMatchingRule_WhenMultipleRulesMatch() {
        PaymentRequest request = new PaymentRequest("4234567890123456", BigDecimal.valueOf(1500), EUR);
        PaymentProvider resolved = resolver.resolve(request);
        assertThat(resolved).isEqualTo(providerA);
    }
}

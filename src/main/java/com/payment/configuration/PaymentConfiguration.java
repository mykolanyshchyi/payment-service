package com.payment.configuration;

import com.payment.model.PaymentRoutingRule;
import com.payment.service.provider.MockPaymentProviderA;
import com.payment.service.provider.MockPaymentProviderB;
import com.payment.service.provider.PaymentProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

@Configuration
public class PaymentConfiguration {

    @Autowired
    public MockPaymentProviderA paymentProviderA;

    @Autowired
    public MockPaymentProviderB paymentProviderB;

    @Bean("defaultPaymentProvider")
    public PaymentProvider defaultPaymentProvider() {
        return paymentProviderA;
    }

    @Bean
    @Scope("prototype")
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public List<PaymentRoutingRule> paymentRoutingRules() {
        return List.of(
                new PaymentRoutingRule(req -> req.bin().startsWith("4"), paymentProviderA), // Visa BIN → A
                new PaymentRoutingRule(req -> req.currency().equals(Currency.getInstance("EUR")), paymentProviderB),
                new PaymentRoutingRule(req -> req.amount().compareTo(new BigDecimal("1000")) > 0, paymentProviderB)
        );
    }
}

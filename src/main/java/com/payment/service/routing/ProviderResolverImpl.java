package com.payment.service.routing;

import com.payment.model.dto.PaymentRequest;
import com.payment.model.PaymentRoutingRule;
import com.payment.service.provider.PaymentProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProviderResolverImpl implements ProviderResolver {

    private final List<PaymentRoutingRule> rules;
    private final PaymentProvider defaultProvider;

    @Autowired
    public ProviderResolverImpl(List<PaymentRoutingRule> rules,
                                @Qualifier("defaultPaymentProvider") PaymentProvider defaultProvider) {
        this.rules = rules;
        this.defaultProvider = defaultProvider;
    }

    @Override
    public PaymentProvider resolve(PaymentRequest request) {
        return rules.stream()
                .filter(r -> r.matches(request))
                .findFirst()
                .map(PaymentRoutingRule::target)
                .orElse(defaultProvider);
    }
}

package com.payment.model;

import com.payment.model.dto.PaymentRequest;
import com.payment.service.provider.PaymentProvider;

import java.util.function.Predicate;

public record PaymentRoutingRule(Predicate<PaymentRequest> condition, PaymentProvider target) {

    public boolean matches(PaymentRequest request) {
        return condition.test(request);
    }
}

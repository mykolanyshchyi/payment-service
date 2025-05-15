package com.payment.service.provider;

import com.payment.model.dto.PaymentProviderResponse;
import com.payment.model.dto.PaymentRequest;

public interface PaymentProvider {

    PaymentProviderResponse pay(PaymentRequest request);
}

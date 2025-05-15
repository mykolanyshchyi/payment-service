package com.payment.service.routing;

import com.payment.model.dto.PaymentRequest;
import com.payment.service.provider.PaymentProvider;

public interface ProviderResolver {

    PaymentProvider resolve(PaymentRequest request);
}

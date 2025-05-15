package com.payment.service;


import com.payment.model.dto.PaymentRequest;
import com.payment.model.dto.PaymentResponse;

public interface PaymentService {

    PaymentResponse makePayment(PaymentRequest request, String idempotencyKey);
}

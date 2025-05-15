package com.payment.model.dto;

import com.payment.model.entity.PaymentStatus;

public record PaymentResponse(String providerId,
                              String transactionId,
                              PaymentStatus status,
                              String errorMessage) {
}

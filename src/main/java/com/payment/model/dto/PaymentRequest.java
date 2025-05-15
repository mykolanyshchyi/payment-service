package com.payment.model.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Currency;

public record PaymentRequest(@NotNull String pan,
                             @NotNull BigDecimal amount,
                             @NotNull Currency currency) {

    public String bin() {
        return pan.substring(0, 6);
    }

    public String maskedPan() {
        return pan.replaceAll(".(?=.{4})", "*");
    }
}

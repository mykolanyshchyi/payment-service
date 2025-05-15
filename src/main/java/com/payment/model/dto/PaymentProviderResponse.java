package com.payment.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentProviderResponse {
    private String transactionId;
    private String status;
    private String message;

    @JsonIgnore
    private String providerId;
}

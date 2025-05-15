package com.payment.controller;

import com.payment.model.dto.PaymentRequest;
import com.payment.model.dto.PaymentResponse;
import com.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Make a payment", description = "Routes the payment to a provider based on BIN, currency or amount.",
            parameters = {
                    @Parameter(name = "Idempotency-Key",
                            description = "Unique key to avoid duplicate charges", in = HEADER, required = true)},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = PaymentRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Payment processed",
                            content = @Content(schema = @Schema(implementation = PaymentResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Validation error"),
                    @ApiResponse(responseCode = "409", description = "Duplicate idempotency key")
            })
    @PostMapping("/api/v1/payments")
    public PaymentResponse create(@Valid @RequestBody PaymentRequest paymentRequest,
                                  @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return paymentService.makePayment(paymentRequest, idempotencyKey);
    }
}

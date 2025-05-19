package com.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.exception.DuplicatePaymentException;
import com.payment.model.dto.PaymentRequest;
import com.payment.model.dto.PaymentResponse;
import com.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Currency;

import static com.payment.model.entity.PaymentStatus.COMPLETED;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final String ENDPOINT = "/api/v1/payments";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnSuccess_WhenPaymentIsSuccessful() throws Exception {
        PaymentRequest request = new PaymentRequest("4242424242424242", BigDecimal.valueOf(100), USD);
        PaymentResponse response = new PaymentResponse("providerA", "txn123", COMPLETED, null);
        String idempotencyKey = "unique-key-123";

        when(paymentService.makePayment(request, idempotencyKey)).thenReturn(response);

        mockMvc.perform(post(ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .header("Idempotency-Key", idempotencyKey)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providerId").value("providerA"))
                .andExpect(jsonPath("$.transactionId").value("txn123"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.errorMessage").doesNotExist());
    }

    @Test
    void shouldReturnDuplicatePaymentException_WhenDuplicateIdempotencyKey() throws Exception {
        PaymentRequest request = new PaymentRequest("4111111111111111", BigDecimal.valueOf(50), USD);
        String idempotencyKey = "duplicate-key-999";

        when(paymentService.makePayment(request, idempotencyKey)).thenThrow(new DuplicatePaymentException());

        mockMvc.perform(post(ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .header("Idempotency-Key", idempotencyKey)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnBadRequest_WhenValidationFails() throws Exception {
        String invalidJson = """
                {
                    "amount": 100,
                    "currency": "USD"
                }
                """;

        mockMvc.perform(post(ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .header("Idempotency-Key", "any")
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}

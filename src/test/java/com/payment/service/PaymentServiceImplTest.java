package com.payment.service;

import com.payment.exception.DuplicatePaymentException;
import com.payment.model.dto.PaymentProviderResponse;
import com.payment.model.dto.PaymentRequest;
import com.payment.model.dto.PaymentResponse;
import com.payment.model.entity.PaymentEntity;
import com.payment.persistence.PaymentRepository;
import com.payment.service.provider.PaymentProvider;
import com.payment.service.routing.ProviderResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;

import static com.payment.model.entity.PaymentStatus.COMPLETED;
import static com.payment.model.entity.PaymentStatus.FAILED;
import static com.payment.model.entity.PaymentStatus.PENDING;
import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ProviderResolver resolver;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void shouldThrowDuplicatePaymentException_WhenIdempotencyKeyExists() {
        String idempotencyKey = "unique-key";
        when(paymentRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(true);

        PaymentRequest request = new PaymentRequest("1234567890123456", TEN, USD);

        assertThatThrownBy(() -> paymentService.makePayment(request, idempotencyKey))
                .isInstanceOf(DuplicatePaymentException.class);

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void shouldMakePaymentSuccessfully() {
        String idempotencyKey = "key-123";
        PaymentRequest request = new PaymentRequest("1234567890123456", BigDecimal.valueOf(100), USD);

        PaymentEntity savedEntity = PaymentEntity.builder()
                .id(1L)
                .idempotencyKey(idempotencyKey)
                .amount(BigDecimal.valueOf(100))
                .currency("USD")
                .status(PENDING)
                .build();

        PaymentProvider provider = mock(PaymentProvider.class);
        PaymentProviderResponse providerResponse = new PaymentProviderResponse("txn123", "success", null, "providerA");

        when(paymentRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(savedEntity);
        when(resolver.resolve(request)).thenReturn(provider);
        when(provider.pay(request)).thenReturn(providerResponse);

        PaymentEntity updatedEntity = PaymentEntity.builder()
                .id(1L)
                .idempotencyKey(idempotencyKey)
                .amount(BigDecimal.valueOf(100))
                .currency("USD")
                .status(COMPLETED)
                .transactionId("txn123")
                .provider("providerA")
                .errorMessage(null)
                .build();

        when(paymentRepository.saveAndFlush(any())).thenReturn(updatedEntity);

        PaymentResponse response = paymentService.makePayment(request, idempotencyKey);

        assertThat(response.providerId()).isEqualTo("providerA");
        assertThat(response.transactionId()).isEqualTo("txn123");
        assertThat(response.status()).isEqualTo(COMPLETED);
        assertThat(response.errorMessage()).isNull();
    }

    @Test
    void shouldMarkPaymentAsFailed_WhenProviderReturnsFailure() {
        String idempotencyKey = "key-999";
        PaymentRequest request = new PaymentRequest("9876543210987654", BigDecimal.valueOf(50), Currency.getInstance("EUR"));

        PaymentEntity savedEntity = PaymentEntity.builder()
                .id(2L)
                .idempotencyKey(idempotencyKey)
                .amount(BigDecimal.valueOf(50))
                .currency("EUR")
                .status(PENDING)
                .build();

        PaymentEntity updatedEntity = PaymentEntity.builder()
                .id(2L)
                .idempotencyKey(idempotencyKey)
                .amount(BigDecimal.valueOf(50))
                .currency("EUR")
                .status(FAILED)
                .transactionId("txn999")
                .provider("providerB")
                .errorMessage("Insufficient funds")
                .build();

        PaymentProvider provider = mock(PaymentProvider.class);
        PaymentProviderResponse providerResponse =
                new PaymentProviderResponse("txn999", "failure", "Insufficient funds", "providerB");

        when(paymentRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(savedEntity);
        when(resolver.resolve(request)).thenReturn(provider);
        when(provider.pay(request)).thenReturn(providerResponse);
        when(paymentRepository.saveAndFlush(any())).thenReturn(updatedEntity);

        PaymentResponse response = paymentService.makePayment(request, idempotencyKey);

        assertThat(response.providerId()).isEqualTo("providerB");
        assertThat(response.transactionId()).isEqualTo("txn999");
        assertThat(response.status()).isEqualTo(FAILED);
        assertThat(response.errorMessage()).isEqualTo("Insufficient funds");
    }
}

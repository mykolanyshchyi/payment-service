package com.payment.service;

import com.payment.exception.DuplicatePaymentException;
import com.payment.model.dto.PaymentProviderResponse;
import com.payment.model.dto.PaymentRequest;
import com.payment.model.dto.PaymentResponse;
import com.payment.model.entity.PaymentEntity;
import com.payment.persistence.PaymentRepository;
import com.payment.service.provider.PaymentProvider;
import com.payment.service.routing.ProviderResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.payment.model.entity.PaymentStatus.COMPLETED;
import static com.payment.model.entity.PaymentStatus.FAILED;
import static com.payment.model.entity.PaymentStatus.PENDING;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ProviderResolver resolver;

    @Override
    @Transactional
    public PaymentResponse makePayment(PaymentRequest request, String idempotencyKey) {
        if (paymentRepository.existsByIdempotencyKey(idempotencyKey)) {
            throw new DuplicatePaymentException();
        }

        PaymentEntity entity = paymentRepository.save(newEntity(request, idempotencyKey));

        PaymentProvider provider = resolver.resolve(request);
        PaymentProviderResponse paymentResponse = provider.pay(request);

        PaymentEntity updatedEntity = updatePaymentEntity(entity, paymentResponse);
        return toResponse(updatedEntity);
    }

    private PaymentEntity updatePaymentEntity(PaymentEntity entity, PaymentProviderResponse paymentResponse) {
        entity.setStatus("success".equals(paymentResponse.getStatus()) ? COMPLETED : FAILED);
        entity.setTransactionId(paymentResponse.getTransactionId());
        entity.setErrorMessage(paymentResponse.getMessage());
        entity.setProvider(paymentResponse.getProviderId());
        return paymentRepository.saveAndFlush(entity);
    }

    private PaymentEntity newEntity(PaymentRequest request, String idempotencyKey) {
        return PaymentEntity.builder()
                .idempotencyKey(idempotencyKey)
                .amount(request.amount())
                .currency(request.currency().getCurrencyCode())
                .status(PENDING)
                .build();
    }

    private PaymentResponse toResponse(PaymentEntity entity) {
        return new PaymentResponse(entity.getProvider(), entity.getTransactionId(), entity.getStatus(), entity.getErrorMessage());
    }
}

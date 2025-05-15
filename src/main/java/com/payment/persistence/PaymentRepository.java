package com.payment.persistence;

import com.payment.model.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    boolean existsByIdempotencyKey(String idempotencyKey);
}

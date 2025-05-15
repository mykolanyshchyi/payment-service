CREATE TYPE payment_status AS ENUM ('PENDING', 'COMPLETED', 'FAILED');

CREATE TABLE IF NOT EXISTS payments
(
    id              bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
    idempotency_key VARCHAR(255),
    transaction_id  VARCHAR(255),
    amount          NUMERIC(19, 4)                      NOT NULL,
    currency        CHAR(3)                             NOT NULL,
    status          payment_status                      NOT NULL,
    error_message   TEXT,
    provider        VARCHAR(255),
    created_at      timestamp                           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      timestamp                           NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT payments_pk PRIMARY KEY (id),
    CONSTRAINT payments_idempotency_key_unique UNIQUE (idempotency_key)
);
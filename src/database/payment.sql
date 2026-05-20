CREATE TABLE payment_orders (
    id              BIGSERIAL       PRIMARY KEY,
    order_code      BIGINT          UNIQUE NOT NULL,
    user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount          INTEGER         NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    checkout_url    TEXT,
    payment_link_id VARCHAR(100),
    expired_at      TIMESTAMPTZ     NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payment_transactions (
    id                    BIGSERIAL       PRIMARY KEY,
    order_code            BIGINT          NOT NULL REFERENCES payment_orders(order_code),
    user_id               BIGINT          NOT NULL REFERENCES users(id),
    amount                INTEGER         NOT NULL,
    reference             VARCHAR(100),
    transaction_date_time VARCHAR(50),
    account_number        VARCHAR(50),
    created_at            TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

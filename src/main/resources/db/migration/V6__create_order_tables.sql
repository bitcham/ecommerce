-- =============================================
-- V6: Order Domain Tables
-- =============================================

-- Orders table (using 'orders' to avoid reserved keyword)
CREATE TABLE orders (
    id                  BIGSERIAL PRIMARY KEY,
    member_id           BIGINT NOT NULL REFERENCES member(id),
    order_number        VARCHAR(30) NOT NULL UNIQUE,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount        DECIMAL(12, 2) NOT NULL,
    discount_amount     DECIMAL(12, 2) NOT NULL DEFAULT 0,
    delivery_fee        DECIMAL(10, 2) NOT NULL DEFAULT 0,
    final_amount        DECIMAL(12, 2) NOT NULL,

    -- Delivery information (snapshot)
    recipient_name      VARCHAR(100) NOT NULL,
    recipient_phone     VARCHAR(20) NOT NULL,
    zip_code            VARCHAR(10) NOT NULL,
    address             VARCHAR(255) NOT NULL,
    address_detail      VARCHAR(255),
    delivery_message    VARCHAR(500),

    ordered_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at             TIMESTAMP,
    cancelled_at        TIMESTAMP,
    cancel_reason       VARCHAR(500),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_order_status CHECK (status IN (
        'PENDING', 'PAID', 'PREPARING', 'SHIPPING', 'DELIVERED',
        'CONFIRMED', 'CANCELLED', 'REFUND_REQUESTED', 'REFUNDED'
    ))
);

CREATE INDEX idx_order_member ON orders(member_id);
CREATE INDEX idx_order_number ON orders(order_number);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_date ON orders(ordered_at);

-- Order item table
CREATE TABLE order_item (
    id                  BIGSERIAL PRIMARY KEY,
    order_id            BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_option_id   BIGINT NOT NULL REFERENCES product_option(id),
    seller_id           BIGINT NOT NULL REFERENCES seller(id),
    product_name        VARCHAR(200) NOT NULL,
    option_name         VARCHAR(100) NOT NULL,
    unit_price          DECIMAL(12, 2) NOT NULL,
    quantity            INT NOT NULL,
    total_price         DECIMAL(12, 2) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_order_item_status CHECK (status IN (
        'PENDING', 'PREPARING', 'SHIPPING', 'DELIVERED', 'CANCELLED', 'REFUNDED'
    ))
);

CREATE INDEX idx_order_item_order ON order_item(order_id);
CREATE INDEX idx_order_item_seller ON order_item(seller_id);
CREATE INDEX idx_order_item_option ON order_item(product_option_id);

-- Payment table
CREATE TABLE payment (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL REFERENCES orders(id),
    payment_key     VARCHAR(200) UNIQUE,
    method          VARCHAR(20) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    amount          DECIMAL(12, 2) NOT NULL,
    paid_at         TIMESTAMP,
    cancelled_at    TIMESTAMP,
    cancel_reason   VARCHAR(500),
    refund_amount   DECIMAL(12, 2),
    pg_provider     VARCHAR(50),
    pg_tid          VARCHAR(100),
    receipt_url     VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_payment_method CHECK (method IN (
        'CARD', 'BANK_TRANSFER', 'VIRTUAL_ACCOUNT', 'KAKAO_PAY', 'TOSS_PAY', 'NAVER_PAY'
    )),
    CONSTRAINT chk_payment_status CHECK (status IN (
        'PENDING', 'COMPLETED', 'FAILED', 'CANCELLED', 'PARTIAL_REFUND', 'FULL_REFUND'
    ))
);

CREATE INDEX idx_payment_order ON payment(order_id);
CREATE INDEX idx_payment_key ON payment(payment_key);

-- Delivery table
CREATE TABLE delivery (
    id                  BIGSERIAL PRIMARY KEY,
    order_id            BIGINT NOT NULL REFERENCES orders(id),
    tracking_number     VARCHAR(50),
    carrier             VARCHAR(50),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    shipped_at          TIMESTAMP,
    delivered_at        TIMESTAMP,
    delivery_type       VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_delivery_status CHECK (status IN (
        'PENDING', 'PREPARING', 'SHIPPED', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED', 'FAILED'
    )),
    CONSTRAINT chk_delivery_type CHECK (delivery_type IN ('NORMAL', 'ROCKET', 'ROCKET_FRESH'))
);

CREATE INDEX idx_delivery_order ON delivery(order_id);
CREATE INDEX idx_delivery_tracking ON delivery(tracking_number);

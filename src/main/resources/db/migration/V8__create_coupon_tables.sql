-- =============================================
-- V8: Coupon Domain Tables
-- =============================================

-- Coupon table
CREATE TABLE coupon (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(50) NOT NULL UNIQUE,
    name                VARCHAR(100) NOT NULL,
    description         TEXT,
    type                VARCHAR(20) NOT NULL,
    discount_value      DECIMAL(12, 2) NOT NULL,
    min_order_amount    DECIMAL(12, 2),
    max_discount_amount DECIMAL(12, 2),
    total_quantity      INT,
    issued_quantity     INT NOT NULL DEFAULT 0,
    starts_at           TIMESTAMP NOT NULL,
    expires_at          TIMESTAMP NOT NULL,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_coupon_type CHECK (type IN ('FIXED_AMOUNT', 'PERCENTAGE')),
    CONSTRAINT chk_coupon_value CHECK (discount_value > 0)
);

CREATE INDEX idx_coupon_code ON coupon(code);
CREATE INDEX idx_coupon_active ON coupon(is_active, starts_at, expires_at);

-- Member coupon table
CREATE TABLE member_coupon (
    id          BIGSERIAL PRIMARY KEY,
    member_id   BIGINT NOT NULL REFERENCES member(id) ON DELETE CASCADE,
    coupon_id   BIGINT NOT NULL REFERENCES coupon(id),
    used_at     TIMESTAMP,
    order_id    BIGINT REFERENCES orders(id),
    expires_at  TIMESTAMP NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_member_coupon UNIQUE (member_id, coupon_id)
);

CREATE INDEX idx_member_coupon_member ON member_coupon(member_id);
CREATE INDEX idx_member_coupon_coupon ON member_coupon(coupon_id);

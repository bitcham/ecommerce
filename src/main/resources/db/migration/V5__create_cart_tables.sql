-- =============================================
-- V5: Cart Domain Tables
-- =============================================

-- Cart table
CREATE TABLE cart (
    id          BIGSERIAL PRIMARY KEY,
    member_id   BIGINT NOT NULL UNIQUE REFERENCES member(id) ON DELETE CASCADE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cart_member ON cart(member_id);

-- Cart item table
CREATE TABLE cart_item (
    id                  BIGSERIAL PRIMARY KEY,
    cart_id             BIGINT NOT NULL REFERENCES cart(id) ON DELETE CASCADE,
    product_option_id   BIGINT NOT NULL REFERENCES product_option(id),
    quantity            INT NOT NULL DEFAULT 1,
    is_selected         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_cart_item UNIQUE (cart_id, product_option_id),
    CONSTRAINT chk_cart_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_cart_item_cart ON cart_item(cart_id);
CREATE INDEX idx_cart_item_option ON cart_item(product_option_id);

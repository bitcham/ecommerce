-- =============================================
-- V7: Review Domain Tables
-- =============================================

-- Review table
CREATE TABLE review (
    id                  BIGSERIAL PRIMARY KEY,
    member_id           BIGINT NOT NULL REFERENCES member(id),
    product_id          BIGINT NOT NULL REFERENCES product(id),
    order_item_id       BIGINT NOT NULL REFERENCES order_item(id),
    rating              INT NOT NULL,
    content             TEXT,
    is_visible          BOOLEAN NOT NULL DEFAULT TRUE,
    seller_reply        TEXT,
    seller_replied_at   TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP,

    CONSTRAINT uq_review_order_item UNIQUE (order_item_id),
    CONSTRAINT chk_review_rating CHECK (rating >= 1 AND rating <= 5)
);

CREATE INDEX idx_review_product ON review(product_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_review_member ON review(member_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_review_order_item ON review(order_item_id);

-- Review image table
CREATE TABLE review_image (
    id          BIGSERIAL PRIMARY KEY,
    review_id   BIGINT NOT NULL REFERENCES review(id) ON DELETE CASCADE,
    url         VARCHAR(500) NOT NULL,
    sort_order  INT NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_review_image_review ON review_image(review_id);

-- Wishlist table
CREATE TABLE wishlist (
    id          BIGSERIAL PRIMARY KEY,
    member_id   BIGINT NOT NULL REFERENCES member(id) ON DELETE CASCADE,
    product_id  BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_wishlist UNIQUE (member_id, product_id)
);

CREATE INDEX idx_wishlist_member ON wishlist(member_id);
CREATE INDEX idx_wishlist_product ON wishlist(product_id);

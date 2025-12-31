-- Wishlist table
CREATE TABLE wishlist (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_wishlist_member_product UNIQUE (member_id, product_id)
);

-- Indexes for performance
CREATE INDEX idx_wishlist_member_id ON wishlist(member_id);
CREATE INDEX idx_wishlist_product_id ON wishlist(product_id);
CREATE INDEX idx_wishlist_created_at ON wishlist(created_at DESC);

COMMENT ON TABLE wishlist IS 'Member wishlist items';
COMMENT ON COLUMN wishlist.member_id IS 'Member who added to wishlist';
COMMENT ON COLUMN wishlist.product_id IS 'Product added to wishlist';

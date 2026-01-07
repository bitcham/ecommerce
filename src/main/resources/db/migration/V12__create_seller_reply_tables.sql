-- =============================================
-- V12: Seller Reply Domain Tables
-- Migrating from embedded fields to separate aggregate
-- =============================================

-- 1. Create seller_reply table
CREATE TABLE seller_reply (
    id              BIGSERIAL PRIMARY KEY,
    review_id       BIGINT NOT NULL REFERENCES review(id),
    seller_id       BIGINT NOT NULL REFERENCES member(id),
    content         TEXT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP,

    CONSTRAINT uq_seller_reply_review UNIQUE (review_id)
);

-- 2. Create seller_reply_history table (audit trail - NO CASCADE)
-- History records must be preserved even if reply is deleted
CREATE TABLE seller_reply_history (
    id                  BIGSERIAL PRIMARY KEY,
    seller_reply_id     BIGINT NOT NULL,  -- No FK constraint for audit preservation
    previous_content    TEXT NOT NULL,
    modified_by         BIGINT NOT NULL REFERENCES member(id),
    modified_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. Create indexes
CREATE INDEX idx_seller_reply_review ON seller_reply(review_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_seller_reply_seller ON seller_reply(seller_id);
CREATE INDEX idx_seller_reply_history_reply ON seller_reply_history(seller_reply_id);
CREATE INDEX idx_seller_reply_history_modified ON seller_reply_history(modified_at DESC);

-- 4. Migrate existing data from review table (if seller_reply exists)
INSERT INTO seller_reply (review_id, seller_id, content, created_at, updated_at)
SELECT
    r.id,
    p.seller_id,
    r.seller_reply,
    COALESCE(r.seller_replied_at, r.created_at),
    COALESCE(r.seller_replied_at, r.updated_at)
FROM review r
JOIN product p ON r.product_id = p.id
WHERE r.seller_reply IS NOT NULL
  AND r.seller_reply != ''
  AND r.deleted_at IS NULL;

-- 5. Drop old columns from review table
ALTER TABLE review DROP COLUMN IF EXISTS seller_reply;
ALTER TABLE review DROP COLUMN IF EXISTS seller_replied_at;

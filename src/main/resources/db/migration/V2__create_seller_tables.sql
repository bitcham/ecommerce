-- =============================================
-- V2: Seller Domain Tables
-- =============================================

-- Seller table
CREATE TABLE seller (
    id                  BIGSERIAL PRIMARY KEY,
    member_id           BIGINT NOT NULL UNIQUE REFERENCES member(id) ON DELETE CASCADE,
    business_name       VARCHAR(200) NOT NULL,
    business_number     VARCHAR(20) NOT NULL UNIQUE,
    representative_name VARCHAR(100) NOT NULL,
    business_address    VARCHAR(500) NOT NULL,
    business_phone      VARCHAR(20) NOT NULL,
    bank_name           VARCHAR(50),
    bank_account        VARCHAR(50),
    bank_holder         VARCHAR(100),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    grade               VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    approved_at         TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_seller_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED')),
    CONSTRAINT chk_seller_grade CHECK (grade IN ('NORMAL', 'POWER', 'ROCKET'))
);

CREATE INDEX idx_seller_member ON seller(member_id);
CREATE INDEX idx_seller_status ON seller(status);
CREATE INDEX idx_seller_business_number ON seller(business_number);

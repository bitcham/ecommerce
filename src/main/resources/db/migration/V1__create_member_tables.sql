-- =============================================
-- V1: Member Domain Tables
-- =============================================

-- Member table
CREATE TABLE member (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    phone           VARCHAR(20),
    profile_image   VARCHAR(500),
    role            VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at   TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP,

    CONSTRAINT chk_member_role CHECK (role IN ('CUSTOMER', 'SELLER', 'ADMIN')),
    CONSTRAINT chk_member_status CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'WITHDRAWN'))
);

CREATE INDEX idx_member_email ON member(email) WHERE deleted_at IS NULL;
CREATE INDEX idx_member_status ON member(status) WHERE deleted_at IS NULL;

-- Member address table
CREATE TABLE member_address (
    id              BIGSERIAL PRIMARY KEY,
    member_id       BIGINT NOT NULL REFERENCES member(id) ON DELETE CASCADE,
    name            VARCHAR(50) NOT NULL,
    recipient_name  VARCHAR(100) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    zip_code        VARCHAR(10) NOT NULL,
    address         VARCHAR(255) NOT NULL,
    address_detail  VARCHAR(255),
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_member_address_member ON member_address(member_id);

-- Email verification token table
CREATE TABLE email_verification_token (
    id          BIGSERIAL PRIMARY KEY,
    member_id   BIGINT NOT NULL REFERENCES member(id) ON DELETE CASCADE,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMP NOT NULL,
    used_at     TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_token ON email_verification_token(token);
CREATE INDEX idx_email_token_member ON email_verification_token(member_id);

-- Refresh token table
CREATE TABLE refresh_token (
    id          BIGSERIAL PRIMARY KEY,
    member_id   BIGINT NOT NULL REFERENCES member(id) ON DELETE CASCADE,
    token       VARCHAR(500) NOT NULL UNIQUE,
    expires_at  TIMESTAMP NOT NULL,
    revoked_at  TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_token ON refresh_token(token) WHERE revoked_at IS NULL;
CREATE INDEX idx_refresh_token_member ON refresh_token(member_id);

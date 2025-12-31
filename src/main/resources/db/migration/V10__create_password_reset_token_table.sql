-- Password reset token table for password recovery

CREATE TABLE password_reset_token (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    member_id BIGINT NOT NULL REFERENCES member(id),
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_password_reset_token_token ON password_reset_token(token);
CREATE INDEX idx_password_reset_token_member ON password_reset_token(member_id);
CREATE INDEX idx_password_reset_token_valid ON password_reset_token(token, is_used, expires_at);

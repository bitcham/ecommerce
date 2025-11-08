-- Create email verification token table
CREATE TABLE email_verification_token (
    id                  BIGSERIAL   PRIMARY KEY,
    token               UUID    NOT NULL UNIQUE,
    member_id           UUID            NOT NULL,
    expires_at          TIMESTAMP       NOT NULL,
    verified            BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP       NOT NULL,

    CONSTRAINT fk_email_verification_member
        FOREIGN KEY (member_id)
        REFERENCES members(id)
        ON DELETE CASCADE
);

-- Create index on token for faster lookups
CREATE INDEX idx_email_verification_token ON email_verification_token(token);

-- Create index on member_id for faster lookups
CREATE INDEX idx_email_verification_member_id ON email_verification_token(member_id);

-- Create index on expires_at for cleanup queries
CREATE INDEX idx_email_verification_expires_at ON email_verification_token(expires_at);
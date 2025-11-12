-- Create member table
CREATE TABLE member (
    id                          UUID            PRIMARY KEY,
    email                       VARCHAR(255)    NOT NULL UNIQUE,
    password_hash               VARCHAR(255)    NOT NULL,
    first_name                  VARCHAR(50)     NOT NULL,
    last_name                   VARCHAR(50)     NOT NULL,
    phone                       VARCHAR(20),
    role                        VARCHAR(20)     NOT NULL DEFAULT 'CUSTOMER',
    status                      VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    billing_street_address      VARCHAR(255),
    billing_city                VARCHAR(100),
    billing_postal_code         VARCHAR(20),
    delivery_street_address     VARCHAR(255),
    delivery_city               VARCHAR(100),
    delivery_postal_code        VARCHAR(20),
    created_at                  TIMESTAMP       NOT NULL,
    updated_at                  TIMESTAMP       NOT NULL
);

-- Create index on email for faster lookups
CREATE INDEX idx_member_email ON member(email);

-- Create index on status for filtering
CREATE INDEX idx_member_status ON member(status);

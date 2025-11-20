-- Create product table
CREATE TABLE product (
    id                      BIGSERIAL           PRIMARY KEY,
    sku                     VARCHAR(100)        NOT NULL UNIQUE,
    name                    VARCHAR(255)        NOT NULL,
    description             TEXT,
    amount                  NUMERIC(19, 2)      NOT NULL,
    image_url               VARCHAR(500),
    product_status          VARCHAR(20)         NOT NULL DEFAULT 'AVAILABLE',
    created_at              TIMESTAMP           NOT NULL,
    updated_at              TIMESTAMP           NOT NULL,
    version                 BIGINT              NOT NULL DEFAULT 0,
    deleted_at              TIMESTAMP
        
    CONSTRAINT chk_product_amount_non_negative CHECK (amount >= 0),
    CONSTRAINT chk_product_status CHECK (product_status IN ('AVAILABLE', 'OUT_OF_STOCK', 'DISCONTINUED'))
);

-- Create index on name for search queries
CREATE INDEX idx_product_name ON product(name);

-- Create index on sku for lookups
CREATE INDEX idx_product_sku ON product(sku);

-- Create index on status for filtering
CREATE INDEX idx_product_status ON product(product_status);

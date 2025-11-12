-- Create product_option table
CREATE TABLE product_option (
    id                      BIGSERIAL           PRIMARY KEY,
    product_id              BIGINT              NOT NULL,
    sku                     VARCHAR(100)        NOT NULL UNIQUE,
    option_name             VARCHAR(255)        NOT NULL,
    stock_quantity          INTEGER             NOT NULL DEFAULT 0,
    created_at              TIMESTAMP           NOT NULL,
    updated_at              TIMESTAMP           NOT NULL,

    CONSTRAINT fk_product_option_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
    CONSTRAINT chk_product_option_stock_non_negative CHECK (stock_quantity >= 0)
);

-- Create index on product_id for joins
CREATE INDEX idx_product_option_product_id ON product_option(product_id);

-- Create index on sku for lookups
CREATE INDEX idx_product_option_sku ON product_option(sku);

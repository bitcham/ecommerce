-- =============================================
-- V4: Product Domain Tables
-- =============================================

-- Product table
CREATE TABLE product (
    id                      BIGSERIAL PRIMARY KEY,
    seller_id               BIGINT NOT NULL REFERENCES seller(id),
    category_id             BIGINT NOT NULL REFERENCES category(id),
    name                    VARCHAR(200) NOT NULL,
    slug                    VARCHAR(250) NOT NULL UNIQUE,
    description             TEXT,
    base_price              DECIMAL(12, 2) NOT NULL,
    discount_price          DECIMAL(12, 2),
    discount_rate           DECIMAL(5, 2),
    status                  VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    delivery_type           VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    delivery_fee            DECIMAL(10, 2) NOT NULL DEFAULT 0,
    free_delivery_threshold DECIMAL(12, 2),
    total_stock             INT NOT NULL DEFAULT 0,
    total_sales             INT NOT NULL DEFAULT 0,
    avg_rating              DECIMAL(2, 1) NOT NULL DEFAULT 0,
    review_count            INT NOT NULL DEFAULT 0,
    view_count              BIGINT NOT NULL DEFAULT 0,
    version                 BIGINT NOT NULL DEFAULT 0,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at              TIMESTAMP,

    CONSTRAINT chk_product_status CHECK (status IN ('DRAFT', 'ACTIVE', 'SOLDOUT', 'SUSPENDED', 'DELETED')),
    CONSTRAINT chk_product_delivery CHECK (delivery_type IN ('NORMAL', 'ROCKET', 'ROCKET_FRESH')),
    CONSTRAINT chk_product_price CHECK (base_price >= 0),
    CONSTRAINT chk_product_discount CHECK (discount_price IS NULL OR discount_price >= 0)
);

CREATE INDEX idx_product_seller ON product(seller_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_product_category ON product(category_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_product_status ON product(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_product_slug ON product(slug) WHERE deleted_at IS NULL;

-- Product option table
CREATE TABLE product_option (
    id               BIGSERIAL PRIMARY KEY,
    product_id       BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    name             VARCHAR(100) NOT NULL,
    sku              VARCHAR(50) NOT NULL UNIQUE,
    additional_price DECIMAL(12, 2) NOT NULL DEFAULT 0,
    stock_quantity   INT NOT NULL DEFAULT 0,
    is_active        BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order       INT NOT NULL DEFAULT 0,
    version          BIGINT NOT NULL DEFAULT 0,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_option_stock CHECK (stock_quantity >= 0)
);

CREATE INDEX idx_product_option_product ON product_option(product_id);
CREATE INDEX idx_product_option_sku ON product_option(sku);

-- Product image table
CREATE TABLE product_image (
    id          BIGSERIAL PRIMARY KEY,
    product_id  BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    url         VARCHAR(500) NOT NULL,
    alt_text    VARCHAR(255),
    is_primary  BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order  INT NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_product_image_product ON product_image(product_id);

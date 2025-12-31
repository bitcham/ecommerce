-- =============================================
-- V3: Category Domain Tables
-- =============================================

-- Category table (hierarchical)
CREATE TABLE category (
    id          BIGSERIAL PRIMARY KEY,
    parent_id   BIGINT REFERENCES category(id) ON DELETE SET NULL,
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(100) NOT NULL UNIQUE,
    depth       INT NOT NULL DEFAULT 0,
    sort_order  INT NOT NULL DEFAULT 0,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_category_parent ON category(parent_id);
CREATE INDEX idx_category_slug ON category(slug);
CREATE INDEX idx_category_active ON category(is_active) WHERE is_active = TRUE;

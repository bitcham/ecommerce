# SPC: Technical Specification Document

## 1. System Architecture

### 1.1 Architecture Overview
```
┌─────────────────────────────────────────────────────────────────────┐
│                          Client Layer                               │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                 │
│  │   Web App   │  │ Mobile App  │  │  Admin App  │                 │
│  │   (React)   │  │   (RN/App)  │  │   (React)   │                 │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘                 │
└─────────┼────────────────┼────────────────┼─────────────────────────┘
          │                │                │
          ▼                ▼                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       API Gateway / Load Balancer                   │
│                    (Rate Limiting, SSL Termination)                 │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       Application Layer                             │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                    Spring Boot Application                    │  │
│  │  ┌─────────────┬─────────────┬─────────────┬──────────────┐  │  │
│  │  │   Member    │   Product   │    Order    │   Payment    │  │  │
│  │  │   Module    │   Module    │   Module    │   Module     │  │  │
│  │  └─────────────┴─────────────┴─────────────┴──────────────┘  │  │
│  │  ┌─────────────┬─────────────┬─────────────┬──────────────┐  │  │
│  │  │   Review    │   Search    │   Coupon    │ Notification │  │  │
│  │  │   Module    │   Module    │   Module    │   Module     │  │  │
│  │  └─────────────┴─────────────┴─────────────┴──────────────┘  │  │
│  └──────────────────────────────────────────────────────────────┘  │
└───────────────┬─────────────────────────────────────────────────────┘
                │
    ┌───────────┼───────────┬───────────────┐
    ▼           ▼           ▼               ▼
┌────────┐ ┌────────┐ ┌───────────┐ ┌─────────────┐
│PostgreSQL│ │ Redis │ │Elasticsearch│ │ File Storage│
│(Primary)│ │(Cache)│ │  (Search)  │ │  (S3/MinIO) │
└────┬────┘ └────────┘ └───────────┘ └─────────────┘
     │
     ▼
┌────────────┐
│ PostgreSQL │
│ (Replica)  │
└────────────┘
```

### 1.2 Package Structure
```
platform.ecommerce
├── EcommerceApplication.java          # Main Application
├── config/                            # Configuration
│   ├── SecurityConfig.java
│   ├── JpaConfig.java
│   ├── RedisConfig.java
│   ├── WebConfig.java
│   ├── OpenApiConfig.java
│   ├── AsyncConfig.java
│   └── properties/
│       ├── JwtProperties.java
│       └── AppProperties.java
│
├── domain/                            # Domain Entities
│   ├── common/
│   │   ├── BaseEntity.java           # id, createdAt, updatedAt
│   │   ├── BaseTimeEntity.java       # createdAt, updatedAt only
│   │   ├── SoftDeletable.java        # deletedAt interface
│   │   └── vo/                        # Value Objects (Immutable)
│   │       ├── Money.java            # 금액 (amount, currency)
│   │       ├── Address.java          # 주소 (zipCode, address, detail)
│   │       ├── Email.java            # 이메일 (validation 포함)
│   │       └── PhoneNumber.java      # 전화번호 (validation 포함)
│   ├── member/
│   │   ├── Member.java
│   │   ├── MemberAddress.java
│   │   └── MemberRole.java (enum)
│   ├── seller/
│   │   ├── Seller.java
│   │   └── SellerStatus.java (enum)
│   ├── product/
│   │   ├── Product.java
│   │   ├── ProductOption.java
│   │   ├── ProductImage.java
│   │   └── ProductStatus.java (enum)
│   ├── category/
│   │   └── Category.java
│   ├── cart/
│   │   ├── Cart.java
│   │   └── CartItem.java
│   ├── order/
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   ├── OrderStatus.java (enum)
│   │   └── OrderCancelReason.java (enum)
│   ├── payment/
│   │   ├── Payment.java
│   │   └── PaymentStatus.java (enum)
│   ├── delivery/
│   │   ├── Delivery.java
│   │   └── DeliveryStatus.java (enum)
│   ├── review/
│   │   ├── Review.java
│   │   └── ReviewImage.java
│   ├── wishlist/
│   │   └── Wishlist.java
│   ├── coupon/
│   │   ├── Coupon.java
│   │   ├── CouponType.java (enum)
│   │   └── MemberCoupon.java
│   └── notification/
│       ├── Notification.java
│       └── NotificationType.java (enum)
│
├── repository/                        # Data Access Layer
│   ├── member/
│   │   ├── MemberRepository.java
│   │   ├── MemberQueryRepository.java
│   │   └── MemberQueryRepositoryImpl.java
│   ├── product/
│   │   ├── ProductRepository.java
│   │   ├── ProductQueryRepository.java
│   │   └── ProductQueryRepositoryImpl.java
│   └── ... (same pattern for other domains)
│
├── service/                           # Business Logic Layer
│   ├── member/
│   │   ├── MemberService.java
│   │   └── MemberServiceImpl.java
│   ├── auth/
│   │   ├── AuthService.java
│   │   └── AuthServiceImpl.java
│   └── ... (same pattern for other domains)
│
├── controller/                        # Presentation Layer
│   ├── member/
│   │   └── MemberController.java
│   ├── auth/
│   │   └── AuthController.java
│   └── ... (same pattern for other domains)
│
├── dto/                               # Data Transfer Objects
│   ├── request/
│   │   ├── member/
│   │   │   ├── MemberCreateRequest.java
│   │   │   ├── MemberUpdateRequest.java
│   │   │   └── LoginRequest.java
│   │   └── ... (same pattern for other domains)
│   └── response/
│       ├── common/
│       │   ├── ApiResponse.java
│       │   └── PageResponse.java
│       ├── member/
│       │   └── MemberResponse.java
│       └── ... (same pattern for other domains)
│
├── mapper/                            # DTO-Entity Mapping (MapStruct)
│   ├── MemberMapper.java
│   ├── ProductMapper.java
│   └── ...
│
├── exception/                         # Exception Handling
│   ├── GlobalExceptionHandler.java
│   ├── ErrorCode.java (enum)
│   ├── BusinessException.java
│   ├── member/
│   │   ├── MemberNotFoundException.java
│   │   └── DuplicateEmailException.java
│   └── ...
│
├── security/                          # Security Components
│   ├── jwt/
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── JwtAuthenticationEntryPoint.java
│   ├── oauth2/
│   │   ├── OAuth2UserService.java
│   │   └── OAuth2SuccessHandler.java
│   └── MemberDetails.java
│
├── event/                             # Domain Events
│   ├── member/
│   │   └── MemberRegisteredEvent.java
│   ├── order/
│   │   └── OrderCreatedEvent.java
│   └── EventPublisher.java
│
├── infrastructure/                    # External Services
│   ├── mail/
│   │   └── EmailService.java
│   ├── storage/
│   │   └── FileStorageService.java
│   ├── payment/
│   │   └── PaymentGatewayClient.java
│   └── search/
│       └── ElasticsearchClient.java
│
└── util/                              # Utility Classes
    ├── SecurityUtils.java
    └── SlugUtils.java
```

---

## 2. Database Design

### 2.1 ERD Overview

```
┌──────────────────────────────────────────────────────────────────────────┐
│                              MEMBER DOMAIN                               │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌─────────────┐         ┌─────────────────┐                            │
│  │   member    │────────<│ member_address  │                            │
│  └──────┬──────┘         └─────────────────┘                            │
│         │                                                                │
│         │  ┌─────────────┐      ┌──────────────┐                        │
│         ├─<│    cart     │─────<│  cart_item   │                        │
│         │  └─────────────┘      └──────────────┘                        │
│         │                                                                │
│         │  ┌─────────────┐      ┌──────────────┐                        │
│         ├─<│   wishlist  │─────>│   product    │                        │
│         │  └─────────────┘      └──────────────┘                        │
│         │                                                                │
│         │  ┌──────────────────┐                                         │
│         └─<│  member_coupon   │                                         │
│            └──────────────────┘                                         │
└──────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│                              SELLER DOMAIN                               │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌─────────────┐         ┌─────────────────┐                            │
│  │   seller    │────────<│    product      │                            │
│  └─────────────┘         └────────┬────────┘                            │
│                                   │                                      │
│         ┌─────────────────────────┼─────────────────────┐               │
│         │                         │                     │               │
│         ▼                         ▼                     ▼               │
│  ┌──────────────┐         ┌──────────────┐      ┌──────────────┐       │
│  │product_option│         │product_image │      │product_category│      │
│  └──────────────┘         └──────────────┘      └──────────────┘       │
└──────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│                              ORDER DOMAIN                                │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  member ──────────────< orders >───────────< order_item                 │
│                           │                      │                       │
│                           │                      ▼                       │
│                           │               product_option                 │
│                           │                                              │
│                           ├──────────< payment                          │
│                           │                                              │
│                           └──────────< delivery                         │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│                             CATEGORY DOMAIN                              │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│         category (self-referencing for hierarchy)                       │
│              │                                                           │
│              └───< category (children)                                  │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

### 2.2 Table Definitions

#### 2.2.1 Member Domain

```sql
-- 회원 테이블
CREATE TABLE member (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    phone           VARCHAR(20),
    profile_image   VARCHAR(500),
    role            VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',  -- CUSTOMER, SELLER, ADMIN
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',   -- PENDING, ACTIVE, SUSPENDED, WITHDRAWN
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

-- 배송지 테이블
CREATE TABLE member_address (
    id              BIGSERIAL PRIMARY KEY,
    member_id       BIGINT NOT NULL REFERENCES member(id),
    name            VARCHAR(50) NOT NULL,           -- 배송지명 (집, 회사 등)
    recipient_name  VARCHAR(100) NOT NULL,          -- 수령인
    recipient_phone VARCHAR(20) NOT NULL,           -- 수령인 연락처
    zip_code        VARCHAR(10) NOT NULL,
    address         VARCHAR(255) NOT NULL,          -- 기본 주소
    address_detail  VARCHAR(255),                   -- 상세 주소
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_member_default_address UNIQUE (member_id, is_default)
        WHERE is_default = TRUE
);

CREATE INDEX idx_member_address_member ON member_address(member_id);

-- 이메일 인증 토큰
CREATE TABLE email_verification_token (
    id          BIGSERIAL PRIMARY KEY,
    member_id   BIGINT NOT NULL REFERENCES member(id),
    token       VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMP NOT NULL,
    used_at     TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_token ON email_verification_token(token);

-- 리프레시 토큰
CREATE TABLE refresh_token (
    id          BIGSERIAL PRIMARY KEY,
    member_id   BIGINT NOT NULL REFERENCES member(id),
    token       VARCHAR(500) NOT NULL UNIQUE,
    expires_at  TIMESTAMP NOT NULL,
    revoked_at  TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_member_active_token UNIQUE (member_id)
        WHERE revoked_at IS NULL
);

CREATE INDEX idx_refresh_token ON refresh_token(token) WHERE revoked_at IS NULL;
```

#### 2.2.2 Seller Domain

```sql
-- 판매자 테이블
CREATE TABLE seller (
    id                  BIGSERIAL PRIMARY KEY,
    member_id           BIGINT NOT NULL UNIQUE REFERENCES member(id),
    business_name       VARCHAR(200) NOT NULL,          -- 상호명
    business_number     VARCHAR(20) NOT NULL UNIQUE,    -- 사업자번호
    representative_name VARCHAR(100) NOT NULL,          -- 대표자명
    business_address    VARCHAR(500) NOT NULL,          -- 사업장 주소
    business_phone      VARCHAR(20) NOT NULL,
    bank_name           VARCHAR(50),
    bank_account        VARCHAR(50),
    bank_holder         VARCHAR(100),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, SUSPENDED
    grade               VARCHAR(20) NOT NULL DEFAULT 'NORMAL',  -- NORMAL, POWER, ROCKET
    approved_at         TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_seller_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED')),
    CONSTRAINT chk_seller_grade CHECK (grade IN ('NORMAL', 'POWER', 'ROCKET'))
);

CREATE INDEX idx_seller_member ON seller(member_id);
CREATE INDEX idx_seller_status ON seller(status);
```

#### 2.2.3 Product Domain

```sql
-- 카테고리 테이블 (계층형)
CREATE TABLE category (
    id          BIGSERIAL PRIMARY KEY,
    parent_id   BIGINT REFERENCES category(id),
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(100) NOT NULL UNIQUE,
    depth       INT NOT NULL DEFAULT 0,             -- 0: 대분류, 1: 중분류, 2: 소분류
    sort_order  INT NOT NULL DEFAULT 0,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_category_parent ON category(parent_id);
CREATE INDEX idx_category_slug ON category(slug);

-- 상품 테이블
CREATE TABLE product (
    id              BIGSERIAL PRIMARY KEY,
    seller_id       BIGINT NOT NULL REFERENCES seller(id),
    category_id     BIGINT NOT NULL REFERENCES category(id),
    name            VARCHAR(200) NOT NULL,
    slug            VARCHAR(250) NOT NULL UNIQUE,
    description     TEXT,
    base_price      DECIMAL(12, 2) NOT NULL,
    discount_price  DECIMAL(12, 2),
    discount_rate   DECIMAL(5, 2),
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- DRAFT, ACTIVE, SOLDOUT, SUSPENDED, DELETED
    delivery_type   VARCHAR(20) NOT NULL DEFAULT 'NORMAL', -- NORMAL, ROCKET, ROCKET_FRESH
    delivery_fee    DECIMAL(10, 2) NOT NULL DEFAULT 0,
    free_delivery_threshold DECIMAL(12, 2),
    total_stock     INT NOT NULL DEFAULT 0,
    total_sales     INT NOT NULL DEFAULT 0,
    avg_rating      DECIMAL(2, 1) NOT NULL DEFAULT 0,
    review_count    INT NOT NULL DEFAULT 0,
    view_count      BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP,

    CONSTRAINT chk_product_status CHECK (status IN ('DRAFT', 'ACTIVE', 'SOLDOUT', 'SUSPENDED', 'DELETED')),
    CONSTRAINT chk_product_delivery CHECK (delivery_type IN ('NORMAL', 'ROCKET', 'ROCKET_FRESH')),
    CONSTRAINT chk_product_price CHECK (base_price >= 0),
    CONSTRAINT chk_product_discount CHECK (discount_price IS NULL OR discount_price >= 0)
);

CREATE INDEX idx_product_seller ON product(seller_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_product_category ON product(category_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_product_status ON product(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_product_slug ON product(slug) WHERE deleted_at IS NULL;

-- 상품 옵션 테이블
CREATE TABLE product_option (
    id              BIGSERIAL PRIMARY KEY,
    product_id      BIGINT NOT NULL REFERENCES product(id),
    name            VARCHAR(100) NOT NULL,          -- 옵션명 (예: "색상: 블랙 / 사이즈: XL")
    sku             VARCHAR(50) NOT NULL UNIQUE,    -- Stock Keeping Unit
    additional_price DECIMAL(12, 2) NOT NULL DEFAULT 0,
    stock_quantity  INT NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order      INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_option_stock CHECK (stock_quantity >= 0)
);

CREATE INDEX idx_product_option_product ON product_option(product_id);
CREATE INDEX idx_product_option_sku ON product_option(sku);

-- 상품 이미지 테이블
CREATE TABLE product_image (
    id          BIGSERIAL PRIMARY KEY,
    product_id  BIGINT NOT NULL REFERENCES product(id),
    url         VARCHAR(500) NOT NULL,
    alt_text    VARCHAR(255),
    is_primary  BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order  INT NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_product_image_product ON product_image(product_id);
```

#### 2.2.4 Cart Domain

```sql
-- 장바구니 테이블
CREATE TABLE cart (
    id          BIGSERIAL PRIMARY KEY,
    member_id   BIGINT NOT NULL UNIQUE REFERENCES member(id),
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 장바구니 아이템 테이블
CREATE TABLE cart_item (
    id                  BIGSERIAL PRIMARY KEY,
    cart_id             BIGINT NOT NULL REFERENCES cart(id),
    product_option_id   BIGINT NOT NULL REFERENCES product_option(id),
    quantity            INT NOT NULL DEFAULT 1,
    is_selected         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_cart_item UNIQUE (cart_id, product_option_id),
    CONSTRAINT chk_cart_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_cart_item_cart ON cart_item(cart_id);
```

#### 2.2.5 Order Domain

```sql
-- 주문 테이블
CREATE TABLE orders (
    id                  BIGSERIAL PRIMARY KEY,
    member_id           BIGINT NOT NULL REFERENCES member(id),
    order_number        VARCHAR(30) NOT NULL UNIQUE,    -- 주문번호 (예: ORD-20251227-XXXXX)
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount        DECIMAL(12, 2) NOT NULL,        -- 총 상품금액
    discount_amount     DECIMAL(12, 2) NOT NULL DEFAULT 0,  -- 할인금액
    delivery_fee        DECIMAL(10, 2) NOT NULL DEFAULT 0,   -- 배송비
    final_amount        DECIMAL(12, 2) NOT NULL,        -- 최종 결제금액

    -- 배송 정보
    recipient_name      VARCHAR(100) NOT NULL,
    recipient_phone     VARCHAR(20) NOT NULL,
    zip_code            VARCHAR(10) NOT NULL,
    address             VARCHAR(255) NOT NULL,
    address_detail      VARCHAR(255),
    delivery_message    VARCHAR(500),

    ordered_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at             TIMESTAMP,
    cancelled_at        TIMESTAMP,
    cancel_reason       VARCHAR(500),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_order_status CHECK (status IN (
        'PENDING',          -- 주문대기 (결제 전)
        'PAID',             -- 결제완료
        'PREPARING',        -- 상품준비중
        'SHIPPING',         -- 배송중
        'DELIVERED',        -- 배송완료
        'CONFIRMED',        -- 구매확정
        'CANCELLED',        -- 주문취소
        'REFUND_REQUESTED', -- 환불요청
        'REFUNDED'          -- 환불완료
    ))
);

CREATE INDEX idx_order_member ON orders(member_id);
CREATE INDEX idx_order_number ON orders(order_number);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_date ON orders(ordered_at);

-- 주문 상품 테이블
CREATE TABLE order_item (
    id                  BIGSERIAL PRIMARY KEY,
    order_id            BIGINT NOT NULL REFERENCES orders(id),
    product_option_id   BIGINT NOT NULL REFERENCES product_option(id),
    seller_id           BIGINT NOT NULL REFERENCES seller(id),
    product_name        VARCHAR(200) NOT NULL,          -- 주문 시점의 상품명 (스냅샷)
    option_name         VARCHAR(100) NOT NULL,          -- 주문 시점의 옵션명 (스냅샷)
    unit_price          DECIMAL(12, 2) NOT NULL,        -- 개당 가격
    quantity            INT NOT NULL,
    total_price         DECIMAL(12, 2) NOT NULL,        -- unit_price * quantity
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_order_item_status CHECK (status IN (
        'PENDING', 'PREPARING', 'SHIPPING', 'DELIVERED', 'CANCELLED', 'REFUNDED'
    ))
);

CREATE INDEX idx_order_item_order ON order_item(order_id);
CREATE INDEX idx_order_item_seller ON order_item(seller_id);
```

#### 2.2.6 Payment Domain

```sql
-- 결제 테이블
CREATE TABLE payment (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL REFERENCES orders(id),
    payment_key     VARCHAR(200) UNIQUE,            -- PG사 결제 키
    method          VARCHAR(20) NOT NULL,           -- CARD, BANK_TRANSFER, VIRTUAL_ACCOUNT, KAKAO_PAY, TOSS_PAY
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    amount          DECIMAL(12, 2) NOT NULL,
    paid_at         TIMESTAMP,
    cancelled_at    TIMESTAMP,
    cancel_reason   VARCHAR(500),
    refund_amount   DECIMAL(12, 2),
    pg_provider     VARCHAR(50),                    -- 토스페이먼츠, 카카오페이 등
    pg_tid          VARCHAR(100),                   -- PG사 거래 ID
    receipt_url     VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_payment_method CHECK (method IN (
        'CARD', 'BANK_TRANSFER', 'VIRTUAL_ACCOUNT', 'KAKAO_PAY', 'TOSS_PAY', 'NAVER_PAY'
    )),
    CONSTRAINT chk_payment_status CHECK (status IN (
        'PENDING', 'COMPLETED', 'FAILED', 'CANCELLED', 'PARTIAL_REFUND', 'FULL_REFUND'
    ))
);

CREATE INDEX idx_payment_order ON payment(order_id);
CREATE INDEX idx_payment_key ON payment(payment_key);
```

#### 2.2.7 Delivery Domain

```sql
-- 배송 테이블
CREATE TABLE delivery (
    id                  BIGSERIAL PRIMARY KEY,
    order_id            BIGINT NOT NULL REFERENCES orders(id),
    tracking_number     VARCHAR(50),                    -- 운송장 번호
    carrier             VARCHAR(50),                    -- 택배사
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    shipped_at          TIMESTAMP,
    delivered_at        TIMESTAMP,
    delivery_type       VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_delivery_status CHECK (status IN (
        'PENDING', 'PREPARING', 'SHIPPED', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED', 'FAILED'
    )),
    CONSTRAINT chk_delivery_type CHECK (delivery_type IN ('NORMAL', 'ROCKET', 'ROCKET_FRESH'))
);

CREATE INDEX idx_delivery_order ON delivery(order_id);
CREATE INDEX idx_delivery_tracking ON delivery(tracking_number);
```

#### 2.2.8 Review Domain

```sql
-- 리뷰 테이블
CREATE TABLE review (
    id              BIGSERIAL PRIMARY KEY,
    member_id       BIGINT NOT NULL REFERENCES member(id),
    product_id      BIGINT NOT NULL REFERENCES product(id),
    order_item_id   BIGINT NOT NULL REFERENCES order_item(id),
    rating          INT NOT NULL,                       -- 1-5
    content         TEXT,
    is_visible      BOOLEAN NOT NULL DEFAULT TRUE,
    seller_reply    TEXT,
    seller_replied_at TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP,

    CONSTRAINT uq_review_order_item UNIQUE (order_item_id),
    CONSTRAINT chk_review_rating CHECK (rating >= 1 AND rating <= 5)
);

CREATE INDEX idx_review_product ON review(product_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_review_member ON review(member_id) WHERE deleted_at IS NULL;

-- 리뷰 이미지 테이블
CREATE TABLE review_image (
    id          BIGSERIAL PRIMARY KEY,
    review_id   BIGINT NOT NULL REFERENCES review(id),
    url         VARCHAR(500) NOT NULL,
    sort_order  INT NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_review_image_review ON review_image(review_id);
```

#### 2.2.9 Wishlist Domain

```sql
-- 찜 테이블
CREATE TABLE wishlist (
    id          BIGSERIAL PRIMARY KEY,
    member_id   BIGINT NOT NULL REFERENCES member(id),
    product_id  BIGINT NOT NULL REFERENCES product(id),
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_wishlist UNIQUE (member_id, product_id)
);

CREATE INDEX idx_wishlist_member ON wishlist(member_id);
CREATE INDEX idx_wishlist_product ON wishlist(product_id);
```

#### 2.2.10 Coupon Domain

```sql
-- 쿠폰 테이블
CREATE TABLE coupon (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(50) NOT NULL UNIQUE,
    name                VARCHAR(100) NOT NULL,
    description         TEXT,
    type                VARCHAR(20) NOT NULL,           -- FIXED_AMOUNT, PERCENTAGE
    discount_value      DECIMAL(12, 2) NOT NULL,        -- 할인액/할인율
    min_order_amount    DECIMAL(12, 2),                 -- 최소 주문금액
    max_discount_amount DECIMAL(12, 2),                 -- 최대 할인금액 (정률인 경우)
    total_quantity      INT,                            -- 총 발급 수량 (null = 무제한)
    issued_quantity     INT NOT NULL DEFAULT 0,         -- 발급된 수량
    starts_at           TIMESTAMP NOT NULL,
    expires_at          TIMESTAMP NOT NULL,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_coupon_type CHECK (type IN ('FIXED_AMOUNT', 'PERCENTAGE')),
    CONSTRAINT chk_coupon_value CHECK (discount_value > 0)
);

CREATE INDEX idx_coupon_code ON coupon(code);

-- 회원 쿠폰 테이블
CREATE TABLE member_coupon (
    id          BIGSERIAL PRIMARY KEY,
    member_id   BIGINT NOT NULL REFERENCES member(id),
    coupon_id   BIGINT NOT NULL REFERENCES coupon(id),
    used_at     TIMESTAMP,
    order_id    BIGINT REFERENCES orders(id),
    expires_at  TIMESTAMP NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_member_coupon UNIQUE (member_id, coupon_id)
);

CREATE INDEX idx_member_coupon_member ON member_coupon(member_id);
```

#### 2.2.11 Notification Domain

```sql
-- 알림 테이블
CREATE TABLE notification (
    id          BIGSERIAL PRIMARY KEY,
    member_id   BIGINT NOT NULL REFERENCES member(id),
    type        VARCHAR(30) NOT NULL,               -- ORDER, DELIVERY, PROMOTION, REVIEW, SYSTEM
    title       VARCHAR(200) NOT NULL,
    content     TEXT NOT NULL,
    link_url    VARCHAR(500),
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    read_at     TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_notification_type CHECK (type IN (
        'ORDER', 'DELIVERY', 'PROMOTION', 'REVIEW', 'SYSTEM', 'COUPON'
    ))
);

CREATE INDEX idx_notification_member ON notification(member_id);
CREATE INDEX idx_notification_unread ON notification(member_id, is_read) WHERE is_read = FALSE;
```

---

## 3. API Specification

### 3.1 Authentication APIs

```
POST   /api/v1/auth/register           # 회원가입
POST   /api/v1/auth/login               # 로그인
POST   /api/v1/auth/logout              # 로그아웃
POST   /api/v1/auth/refresh             # 토큰 갱신
POST   /api/v1/auth/email/verify        # 이메일 인증
POST   /api/v1/auth/password/reset      # 비밀번호 재설정 요청
PUT    /api/v1/auth/password/reset      # 비밀번호 재설정 확인
POST   /api/v1/auth/oauth2/{provider}   # 소셜 로그인
```

### 3.2 Member APIs

```
GET    /api/v1/members/me               # 내 정보 조회
PUT    /api/v1/members/me               # 내 정보 수정
DELETE /api/v1/members/me               # 회원 탈퇴
PUT    /api/v1/members/me/password      # 비밀번호 변경

# 배송지 관리
GET    /api/v1/members/me/addresses     # 배송지 목록
POST   /api/v1/members/me/addresses     # 배송지 추가
PUT    /api/v1/members/me/addresses/{id} # 배송지 수정
DELETE /api/v1/members/me/addresses/{id} # 배송지 삭제
```

### 3.3 Seller APIs

```
POST   /api/v1/sellers/register         # 판매자 등록 신청
GET    /api/v1/sellers/me               # 판매자 정보 조회
PUT    /api/v1/sellers/me               # 판매자 정보 수정

# 판매자 상품 관리
GET    /api/v1/sellers/me/products      # 내 상품 목록
POST   /api/v1/sellers/me/products      # 상품 등록
PUT    /api/v1/sellers/me/products/{id} # 상품 수정
DELETE /api/v1/sellers/me/products/{id} # 상품 삭제

# 판매자 주문 관리
GET    /api/v1/sellers/me/orders        # 내 주문 목록
PUT    /api/v1/sellers/me/orders/{id}/status # 주문 상태 변경
```

### 3.4 Product APIs

```
GET    /api/v1/products                 # 상품 목록 (검색/필터/페이징)
GET    /api/v1/products/{id}            # 상품 상세
GET    /api/v1/products/{id}/options    # 상품 옵션 목록
GET    /api/v1/products/{id}/reviews    # 상품 리뷰 목록
GET    /api/v1/products/slug/{slug}     # 슬러그로 상품 조회
```

### 3.5 Category APIs

```
GET    /api/v1/categories               # 카테고리 목록 (트리 구조)
GET    /api/v1/categories/{id}          # 카테고리 상세
GET    /api/v1/categories/{id}/products # 카테고리별 상품 목록
```

### 3.6 Cart APIs

```
GET    /api/v1/cart                     # 장바구니 조회
POST   /api/v1/cart/items               # 상품 추가
PUT    /api/v1/cart/items/{id}          # 수량 변경
DELETE /api/v1/cart/items/{id}          # 상품 삭제
DELETE /api/v1/cart/items               # 선택 삭제
PUT    /api/v1/cart/items/{id}/select   # 선택/해제 토글
```

### 3.7 Order APIs

```
POST   /api/v1/orders                   # 주문 생성
GET    /api/v1/orders                   # 주문 목록
GET    /api/v1/orders/{id}              # 주문 상세
POST   /api/v1/orders/{id}/cancel       # 주문 취소
POST   /api/v1/orders/{id}/confirm      # 구매 확정

# 반품/교환
POST   /api/v1/orders/{id}/refund       # 환불 요청
```

### 3.8 Payment APIs

```
POST   /api/v1/payments/prepare         # 결제 준비 (PG 연동용)
POST   /api/v1/payments/confirm         # 결제 승인
GET    /api/v1/payments/{id}            # 결제 상세
POST   /api/v1/payments/{id}/cancel     # 결제 취소
```

### 3.9 Review APIs

```
POST   /api/v1/reviews                  # 리뷰 작성
PUT    /api/v1/reviews/{id}             # 리뷰 수정
DELETE /api/v1/reviews/{id}             # 리뷰 삭제
GET    /api/v1/members/me/reviews       # 내 리뷰 목록

# 판매자
POST   /api/v1/sellers/reviews/{id}/reply # 리뷰 답변
```

### 3.10 Wishlist APIs

```
GET    /api/v1/wishlist                 # 찜 목록
POST   /api/v1/wishlist                 # 찜 추가
DELETE /api/v1/wishlist/{productId}     # 찜 삭제
```

### 3.11 Coupon APIs

```
GET    /api/v1/coupons                  # 사용 가능한 쿠폰 목록
GET    /api/v1/members/me/coupons       # 내 쿠폰 목록
POST   /api/v1/coupons/{code}/claim     # 쿠폰 발급
```

### 3.12 Notification APIs

```
GET    /api/v1/notifications            # 알림 목록
PUT    /api/v1/notifications/{id}/read  # 읽음 처리
PUT    /api/v1/notifications/read-all   # 모두 읽음 처리
GET    /api/v1/notifications/unread-count # 읽지 않은 알림 수
```

### 3.13 Search APIs

```
GET    /api/v1/search                   # 통합 검색
GET    /api/v1/search/suggestions       # 검색어 자동완성
GET    /api/v1/search/popular           # 인기 검색어
```

### 3.14 Admin APIs

```
# 회원 관리
GET    /api/v1/admin/members            # 회원 목록
GET    /api/v1/admin/members/{id}       # 회원 상세
PUT    /api/v1/admin/members/{id}/status # 회원 상태 변경

# 판매자 관리
GET    /api/v1/admin/sellers            # 판매자 목록
PUT    /api/v1/admin/sellers/{id}/approve # 판매자 승인/거절

# 상품 관리
GET    /api/v1/admin/products           # 상품 목록
PUT    /api/v1/admin/products/{id}/status # 상품 상태 변경

# 카테고리 관리
POST   /api/v1/admin/categories         # 카테고리 생성
PUT    /api/v1/admin/categories/{id}    # 카테고리 수정
DELETE /api/v1/admin/categories/{id}    # 카테고리 삭제

# 쿠폰 관리
POST   /api/v1/admin/coupons            # 쿠폰 생성
PUT    /api/v1/admin/coupons/{id}       # 쿠폰 수정

# 주문 관리
GET    /api/v1/admin/orders             # 전체 주문 목록

# 통계
GET    /api/v1/admin/stats/dashboard    # 대시보드 통계
GET    /api/v1/admin/stats/sales        # 매출 통계
```

---

## 4. Security Specification

### 4.1 Authentication Flow

```
┌──────────────────────────────────────────────────────────────────────────┐
│                        JWT Authentication Flow                          │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   Client                 API Gateway              Auth Service           │
│     │                        │                         │                 │
│     │── POST /auth/login ───>│                         │                 │
│     │                        │── Validate Credentials ─>│                 │
│     │                        │<── Issue JWT + Refresh ──│                 │
│     │<── 200 + Tokens ───────│                         │                 │
│     │                        │                         │                 │
│     │── GET /api/** ────────>│                         │                 │
│     │   (Authorization:      │── Validate JWT ────────>│                 │
│     │    Bearer <token>)     │<── Valid ───────────────│                 │
│     │                        │── Forward Request ──────>│                 │
│     │<── 200 + Response ─────│                         │                 │
│     │                        │                         │                 │
│     │── POST /auth/refresh ─>│                         │                 │
│     │   (Refresh Token)      │── Validate Refresh ────>│                 │
│     │                        │<── New JWT + Refresh ───│                 │
│     │<── 200 + New Tokens ───│   (Rotate Refresh)      │                 │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

### 4.2 JWT Token Structure

```json
// Access Token (15분)
{
  "sub": "member-uuid",
  "email": "user@example.com",
  "role": "CUSTOMER",
  "iat": 1703660400,
  "exp": 1703661300
}

// Refresh Token (7일) - DB 저장
{
  "sub": "member-uuid",
  "type": "refresh",
  "jti": "unique-token-id",
  "iat": 1703660400,
  "exp": 1704265200
}
```

### 4.3 Rate Limiting Configuration

```yaml
resilience4j:
  ratelimiter:
    instances:
      # 일반 API
      default:
        limitForPeriod: 100
        limitRefreshPeriod: 1s
        timeoutDuration: 0s

      # 인증 API (Brute Force 방지)
      auth:
        limitForPeriod: 10
        limitRefreshPeriod: 1m
        timeoutDuration: 0s

      # 검색 API
      search:
        limitForPeriod: 30
        limitRefreshPeriod: 1s
        timeoutDuration: 0s
```

### 4.4 Security Headers

```java
// SecurityConfig.java
http.headers(headers -> headers
    .contentTypeOptions(withDefaults())
    .xssProtection(withDefaults())
    .cacheControl(withDefaults())
    .frameOptions(FrameOptionsConfig::deny)
    .contentSecurityPolicy(csp ->
        csp.policyDirectives("default-src 'self'"))
);
```

---

## 5. Performance Specification

### 5.1 Caching Strategy

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          Caching Architecture                           │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌───────────────┐     ┌───────────────┐     ┌───────────────┐         │
│  │  Application  │────>│     Redis     │────>│  PostgreSQL   │         │
│  │    (L1)       │     │     (L2)      │     │   (Source)    │         │
│  └───────────────┘     └───────────────┘     └───────────────┘         │
│                                                                         │
│  Cache Keys:                                                            │
│  - product:{id}            TTL: 1h                                     │
│  - product:list:{hash}     TTL: 5m                                     │
│  - category:tree           TTL: 24h                                    │
│  - member:{id}             TTL: 30m                                    │
│  - cart:{memberId}         TTL: 24h                                    │
│  - session:{token}         TTL: 15m                                    │
│                                                                         │
│  Cache Invalidation:                                                    │
│  - Write-through for critical data                                     │
│  - Event-driven invalidation via Spring Events                         │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 5.2 Database Optimization

```sql
-- Read Replica 사용
-- application.yml
spring:
  datasource:
    primary:
      url: jdbc:postgresql://primary:5432/ecommerce
    replica:
      url: jdbc:postgresql://replica:5432/ecommerce

-- @Transactional(readOnly = true)인 경우 Replica로 라우팅
```

### 5.3 Query Optimization Rules

1. **N+1 Problem Prevention**: Fetch Join 또는 @EntityGraph 사용
2. **Pagination**: Offset 대신 Cursor-based pagination (대량 데이터)
3. **Index Strategy**: 자주 조회되는 컬럼에 적절한 인덱스
4. **Query Projection**: 필요한 컬럼만 조회 (DTO Projection)

### 5.4 Concurrency Control (동시성 제어)

#### 5.4.1 Optimistic Locking (낙관적 락)
```java
// 재고 차감, 주문 상태 변경 등
@Entity
public class ProductOption {
    @Version
    private Long version;

    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new InsufficientStockException();
        }
        this.stockQuantity -= quantity;
    }
}
```

#### 5.4.2 Pessimistic Locking (비관적 락)
```java
// 동시 주문이 많은 인기 상품
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT po FROM ProductOption po WHERE po.id = :id")
Optional<ProductOption> findByIdWithLock(@Param("id") Long id);
```

#### 5.4.3 Distributed Lock (분산 락)
```java
// 쿠폰 발급, 선착순 이벤트
@DistributedLock(key = "'coupon:' + #couponId")
public void claimCoupon(Long memberId, Long couponId) {
    // Redis 기반 분산 락 (Redisson)
}
```

### 5.5 Transaction Boundaries (트랜잭션 경계)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      Transaction Boundary Rules                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. Service Layer = Transaction Boundary                               │
│     └── @Transactional은 Service 레이어에서만 선언                      │
│                                                                         │
│  2. Read-Only Transactions                                              │
│     └── @Transactional(readOnly = true) 조회 작업에 적용               │
│                                                                         │
│  3. Transaction Propagation                                             │
│     ├── REQUIRED (기본): 기존 트랜잭션 참여 또는 새로 생성              │
│     ├── REQUIRES_NEW: 항상 새 트랜잭션 (결제 로그 등)                  │
│     └── NOT_SUPPORTED: 트랜잭션 없이 실행 (외부 API 호출)             │
│                                                                         │
│  4. Exception Handling                                                  │
│     ├── RuntimeException: 자동 롤백                                    │
│     ├── CheckedException: 롤백 안됨 (rollbackFor 명시 필요)           │
│     └── BusinessException: 명시적 롤백 설정                            │
│                                                                         │
│  5. Event Publishing                                                    │
│     └── @TransactionalEventListener(phase = AFTER_COMMIT)             │
│         이메일, 알림 등은 커밋 후 발행                                  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 5.6 Idempotency (멱등성 보장)

```java
// 주문 생성 시 멱등성 키 사용
@PostMapping("/orders")
public ApiResponse<OrderResponse> createOrder(
    @RequestHeader("Idempotency-Key") String idempotencyKey,
    @RequestBody OrderCreateRequest request
) {
    return orderService.createOrderIdempotent(idempotencyKey, request);
}

// 중복 요청 방지 테이블
CREATE TABLE idempotency_key (
    key         VARCHAR(100) PRIMARY KEY,
    response    JSONB,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at  TIMESTAMP NOT NULL
);
```

---

## 6. Error Handling Specification

### 6.1 Error Code Structure

```java
public enum ErrorCode {
    // Common (1xxx)
    INVALID_INPUT(1001, "Invalid input"),
    RESOURCE_NOT_FOUND(1002, "Resource not found"),
    UNAUTHORIZED(1003, "Unauthorized access"),
    FORBIDDEN(1004, "Access forbidden"),

    // Auth (2xxx)
    INVALID_CREDENTIALS(2001, "Invalid email or password"),
    TOKEN_EXPIRED(2002, "Token has expired"),
    TOKEN_INVALID(2003, "Invalid token"),
    EMAIL_NOT_VERIFIED(2004, "Email not verified"),
    EMAIL_ALREADY_EXISTS(2005, "Email already registered"),

    // Member (3xxx)
    MEMBER_NOT_FOUND(3001, "Member not found"),
    MEMBER_SUSPENDED(3002, "Member account is suspended"),

    // Product (4xxx)
    PRODUCT_NOT_FOUND(4001, "Product not found"),
    PRODUCT_SOLD_OUT(4002, "Product is sold out"),
    INSUFFICIENT_STOCK(4003, "Insufficient stock"),

    // Order (5xxx)
    ORDER_NOT_FOUND(5001, "Order not found"),
    ORDER_CANNOT_CANCEL(5002, "Order cannot be cancelled"),
    ORDER_ALREADY_PAID(5003, "Order is already paid"),

    // Payment (6xxx)
    PAYMENT_FAILED(6001, "Payment failed"),
    PAYMENT_ALREADY_COMPLETED(6002, "Payment already completed"),

    // Cart (7xxx)
    CART_ITEM_NOT_FOUND(7001, "Cart item not found"),
    CART_EMPTY(7002, "Cart is empty"),

    // Coupon (8xxx)
    COUPON_NOT_FOUND(8001, "Coupon not found"),
    COUPON_EXPIRED(8002, "Coupon has expired"),
    COUPON_ALREADY_USED(8003, "Coupon already used"),

    // System (9xxx)
    INTERNAL_ERROR(9001, "Internal server error"),
    SERVICE_UNAVAILABLE(9002, "Service temporarily unavailable"),
    EXTERNAL_API_ERROR(9003, "External service error");
}
```

### 6.2 Exception Hierarchy

```
BusinessException (abstract)
├── AuthenticationException
│   ├── InvalidCredentialsException
│   ├── TokenExpiredException
│   └── EmailNotVerifiedException
├── ResourceNotFoundException
│   ├── MemberNotFoundException
│   ├── ProductNotFoundException
│   └── OrderNotFoundException
├── ValidationException
│   ├── DuplicateEmailException
│   └── InsufficientStockException
├── AuthorizationException
│   └── ForbiddenAccessException
└── ExternalServiceException
    ├── PaymentGatewayException
    └── EmailServiceException
```

---

## 7. Monitoring Specification

### 7.1 Health Check Endpoints

```
GET /actuator/health              # 전체 헬스
GET /actuator/health/liveness     # 라이브니스 (컨테이너 생존)
GET /actuator/health/readiness    # 레디니스 (트래픽 수용 준비)
```

### 7.2 Metrics

```
# Custom Business Metrics
ecommerce_orders_total{status}           # 주문 수
ecommerce_payments_total{method,status}  # 결제 수
ecommerce_active_users                   # 활성 사용자 수
ecommerce_product_views_total            # 상품 조회수

# Standard Metrics (Micrometer)
http_server_requests_seconds             # HTTP 요청 시간
jvm_memory_used_bytes                    # JVM 메모리
hikaricp_connections_active              # DB 커넥션
```

### 7.3 Logging Format

```json
{
  "timestamp": "2025-12-27T10:00:00.000Z",
  "level": "INFO",
  "logger": "platform.ecommerce.service.OrderService",
  "message": "Order created successfully",
  "traceId": "abc123",
  "spanId": "def456",
  "memberId": 12345,
  "orderId": 67890,
  "orderNumber": "ORD-20251227-00001"
}
```

---

## 8. Deployment & Operations Specification

### 8.1 Deployment Strategy

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      Blue-Green Deployment                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Load Balancer                                                          │
│       │                                                                 │
│       ├──── Blue (Current Production)  ← 트래픽                        │
│       │     └── v1.0.0                                                  │
│       │                                                                 │
│       └──── Green (New Version)        ← 대기                          │
│             └── v1.1.0                                                  │
│                                                                         │
│  Deployment Steps:                                                      │
│  1. Green 환경에 새 버전 배포                                           │
│  2. 헬스체크 및 스모크 테스트                                            │
│  3. 트래픽을 Green으로 전환                                             │
│  4. 모니터링 (10분)                                                     │
│  5. 이상 발생 시 Blue로 롤백                                            │
│  6. 안정화 후 Blue 정리                                                  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 8.2 Database Migration Strategy

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     Zero-Downtime Migration                             │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Phase 1: Expand (확장)                                                 │
│  ├── 새 컬럼 추가 (nullable 또는 default value)                        │
│  ├── 새 테이블 추가                                                     │
│  └── 인덱스 CONCURRENTLY 생성                                          │
│                                                                         │
│  Phase 2: Migrate (마이그레이션)                                        │
│  ├── 기존 데이터를 새 구조로 복사                                       │
│  ├── 애플리케이션에서 dual-write 시작                                   │
│  └── 백그라운드 배치로 데이터 동기화                                    │
│                                                                         │
│  Phase 3: Contract (축소)                                               │
│  ├── 기존 컬럼/테이블 제거                                              │
│  └── 사용하지 않는 인덱스 삭제                                          │
│                                                                         │
│  Rules:                                                                 │
│  - 절대 컬럼 이름 변경 직접 X → 새 컬럼 생성 후 데이터 이관             │
│  - 절대 NOT NULL 제약 직접 추가 X → default 설정 후 추가                │
│  - 큰 테이블 인덱스는 CONCURRENTLY 옵션                                 │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 8.3 Scaling Strategy

```yaml
# Horizontal Pod Autoscaler (HPA) 설정
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: ecommerce-api
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: ecommerce-api
  minReplicas: 2
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
```

### 8.4 Backup & Recovery

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      Backup Strategy                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  PostgreSQL Backup:                                                     │
│  ├── 매일 03:00 UTC - Full Backup (pg_dump)                            │
│  ├── 매시간 - WAL (Write-Ahead Log) 아카이빙                            │
│  ├── PITR (Point-in-Time Recovery) 지원                                │
│  └── 백업 보관 기간: 30일                                               │
│                                                                         │
│  Redis Backup:                                                          │
│  ├── RDB 스냅샷: 매 6시간                                               │
│  ├── AOF (Append Only File): 활성화                                     │
│  └── 세션/캐시 데이터이므로 손실 허용                                   │
│                                                                         │
│  File Storage (S3/MinIO):                                               │
│  ├── 버전닝 활성화                                                      │
│  ├── Cross-Region Replication (재해 복구)                               │
│  └── Lifecycle Policy: 90일 후 Glacier                                  │
│                                                                         │
│  Recovery Time Objective (RTO): 4시간                                   │
│  Recovery Point Objective (RPO): 1시간                                  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 8.5 Incident Response (장애 대응)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      Incident Severity Levels                           │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Severity 1 (Critical):                                                 │
│  ├── 전체 서비스 중단                                                   │
│  ├── 결제 시스템 장애                                                   │
│  ├── 데이터 유출/보안 사고                                              │
│  └── 응답 시간: 15분 이내, 해결 목표: 1시간                             │
│                                                                         │
│  Severity 2 (High):                                                     │
│  ├── 주요 기능 장애 (로그인, 주문)                                      │
│  ├── 성능 심각 저하 (응답 5초 이상)                                     │
│  └── 응답 시간: 30분 이내, 해결 목표: 4시간                             │
│                                                                         │
│  Severity 3 (Medium):                                                   │
│  ├── 부분 기능 장애 (알림, 리뷰)                                        │
│  ├── 성능 저하 (응답 2초 이상)                                          │
│  └── 응답 시간: 2시간 이내, 해결 목표: 24시간                           │
│                                                                         │
│  Severity 4 (Low):                                                      │
│  ├── 마이너 버그                                                        │
│  ├── UI 이슈                                                            │
│  └── 응답 시간: 24시간 이내, 해결 목표: 1주일                           │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 8.6 Alerting Rules

```yaml
# Prometheus Alert Rules
groups:
  - name: ecommerce-alerts
    rules:
      # API 응답시간 경고
      - alert: HighApiLatency
        expr: histogram_quantile(0.99, http_server_requests_seconds_bucket) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "API P99 latency > 1s"

      # 에러율 경고
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) /
              rate(http_server_requests_seconds_count[5m]) > 0.01
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Error rate > 1%"

      # DB 커넥션 풀 경고
      - alert: DbConnectionPoolExhausted
        expr: hikaricp_connections_active / hikaricp_connections_max > 0.9
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "DB connection pool usage > 90%"

      # 결제 실패율 경고
      - alert: HighPaymentFailureRate
        expr: rate(ecommerce_payments_total{status="FAILED"}[10m]) /
              rate(ecommerce_payments_total[10m]) > 0.05
        for: 10m
        labels:
          severity: critical
        annotations:
          summary: "Payment failure rate > 5%"
```

---

## 9. Security Audit Checklist

### 9.1 OWASP Top 10 Compliance

| Vulnerability | Mitigation | Status |
|--------------|------------|--------|
| A01: Broken Access Control | Role-based access, @PreAuthorize | ⬜ |
| A02: Cryptographic Failures | AES-256, BCrypt, TLS 1.3 | ⬜ |
| A03: Injection | Parameterized queries, Input validation | ⬜ |
| A04: Insecure Design | Threat modeling, Security review | ⬜ |
| A05: Security Misconfiguration | Security headers, Hardened config | ⬜ |
| A06: Vulnerable Components | Dependency scanning, SBOM | ⬜ |
| A07: Auth Failures | MFA, Rate limiting, Secure session | ⬜ |
| A08: Data Integrity Failures | Digital signatures, Integrity checks | ⬜ |
| A09: Logging Failures | Structured logging, Audit trail | ⬜ |
| A10: SSRF | URL validation, Allowlist | ⬜ |

### 9.2 Security Headers Checklist

```http
# Required Response Headers
Content-Security-Policy: default-src 'self'; script-src 'self'
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains
Referrer-Policy: strict-origin-when-cross-origin
Permissions-Policy: geolocation=(), microphone=(), camera=()
```

### 9.3 Data Protection

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      PII Data Classification                            │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Level 1 (Highly Sensitive): 암호화 필수 + 마스킹                       │
│  ├── 비밀번호 (해시)                                                    │
│  ├── 신용카드 번호                                                      │
│  ├── 주민등록번호                                                       │
│  └── 은행 계좌번호                                                      │
│                                                                         │
│  Level 2 (Sensitive): 암호화 권장 + 접근 제어                          │
│  ├── 이메일                                                             │
│  ├── 전화번호                                                           │
│  ├── 주소                                                               │
│  └── 생년월일                                                           │
│                                                                         │
│  Level 3 (Internal): 접근 제어                                          │
│  ├── 주문 이력                                                          │
│  ├── 구매 금액                                                          │
│  └── 배송 정보                                                          │
│                                                                         │
│  Logging Rules:                                                         │
│  - Level 1 데이터는 절대 로그에 기록 금지                               │
│  - Level 2 데이터는 마스킹 후 로그 가능 (te***@example.com)             │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Document Info
- **Version**: 1.1.0
- **Created**: 2025-12-27
- **Updated**: 2025-12-27
- **Author**: AI Assistant
- **Reviewed By**: Senior Developer, Tech Lead
- **Status**: Reviewed

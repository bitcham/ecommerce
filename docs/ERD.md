# Entity Relationship Diagram (ERD)

**Project:** E-Commerce Platform
**Last Updated:** 2025-10-21
**Design Pattern:** Domain-Driven Design with Value Objects

---

## Design Notes

### Address as Value Object
Addresses are implemented as **embedded Value Objects** using JPA `@Embeddable`:
- **Members table**: Contains embedded `billingAddress` and `deliveryAddress` fields
- **Orders table**: Contains JSONB snapshots of addresses at order time
- **No separate addresses table**: Addresses have no identity, only attributes
- **Benefits**: Simpler schema, true DDD Value Object pattern, type-safe access

---

## Phase 1 - Complete ERD

```mermaid
erDiagram
    %% Product Catalog Context
    categories ||--o{ categories : "parent-child"
    categories ||--o{ product_categories : "has"
    products ||--o{ product_categories : "belongs to"
    products ||--o{ product_variants : "has"
    products ||--o{ product_images : "has"
    products ||--o{ inventory : "tracks"
    product_variants ||--o| inventory : "tracks"

    %% Member Management Context
    members ||--o{ orders : "places"
    members ||--o| carts : "has"
    members ||--o{ reviews : "writes"
    members ||--o| wishlists : "has"

    %% Shopping Cart Context
    carts ||--o{ cart_items : "contains"
    cart_items }o--|| products : "references"
    cart_items }o--o| product_variants : "references"

    %% Order Management Context
    orders ||--o{ order_items : "contains"
    orders ||--o{ payments : "has"
    order_items }o--|| products : "references"
    order_items }o--o| product_variants : "references"

    %% Review Context
    products ||--o{ reviews : "has"
    orders ||--o{ reviews : "verifies"

    %% Wishlist Context
    wishlists ||--o{ wishlist_items : "contains"
    wishlist_items }o--|| products : "references"
    wishlist_items }o--o| product_variants : "references"

    categories {
        bigserial id PK
        varchar name
        varchar slug UK
        text description
        bigint parent_id FK
        varchar image_url
        integer display_order
        boolean is_active
        timestamp created_at
        timestamp updated_at
    }

    products {
        bigserial id PK
        varchar name
        varchar slug UK
        text description
        text specifications
        varchar brand
        decimal base_price
        varchar status
        timestamp created_at
        timestamp updated_at
        bigint created_by FK
        bigint updated_by FK
    }

    product_categories {
        bigint product_id FK
        bigint category_id FK
        timestamp created_at
    }

    product_variants {
        bigserial id PK
        bigint product_id FK
        varchar sku UK
        varchar name
        jsonb attributes
        decimal price
        boolean is_active
        timestamp created_at
        timestamp updated_at
    }

    product_images {
        bigserial id PK
        bigint product_id FK
        varchar image_url
        varchar alt_text
        integer display_order
        timestamp created_at
    }

    members {
        uuid id PK
        varchar email UK
        varchar password_hash
        varchar first_name
        varchar last_name
        varchar phone
        varchar role
        varchar status
        boolean email_verified
        timestamp email_verified_at
        timestamp last_login_at
        varchar billing_street_address
        varchar billing_city
        varchar billing_postal_code
        varchar delivery_street_address
        varchar delivery_city
        varchar delivery_postal_code
        timestamp created_at
        timestamp updated_at
    }

    carts {
        bigserial id PK
        uuid member_id FK
        varchar session_id UK
        timestamp expires_at
        timestamp created_at
        timestamp updated_at
    }

    cart_items {
        bigserial id PK
        bigint cart_id FK
        bigint product_id FK
        bigint variant_id FK
        integer quantity
        decimal price_at_addition
        timestamp created_at
        timestamp updated_at
    }

    orders {
        bigserial id PK
        varchar order_number UK
        uuid member_id FK
        varchar status
        decimal subtotal
        decimal tax
        decimal shipping_cost
        decimal discount
        decimal total
        varchar currency
        jsonb shipping_address_snapshot
        jsonb billing_address_snapshot
        varchar customer_email
        varchar customer_name
        text notes
        timestamp ordered_at
        timestamp confirmed_at
        timestamp shipped_at
        timestamp delivered_at
        timestamp cancelled_at
        timestamp created_at
        timestamp updated_at
    }

    order_items {
        bigserial id PK
        bigint order_id FK
        bigint product_id FK
        bigint variant_id FK
        jsonb product_snapshot
        integer quantity
        decimal unit_price
        decimal subtotal
        timestamp created_at
    }

    inventory {
        bigserial id PK
        bigint product_id FK
        bigint variant_id FK
        integer quantity
        integer reserved_quantity
        integer reorder_point
        timestamp last_restocked_at
        timestamp created_at
        timestamp updated_at
    }

    payments {
        bigserial id PK
        bigint order_id FK
        varchar payment_method
        decimal amount
        varchar currency
        varchar status
        varchar transaction_id UK
        jsonb gateway_response
        text failure_reason
        timestamp paid_at
        timestamp created_at
        timestamp updated_at
    }

    reviews {
        bigserial id PK
        bigint product_id FK
        uuid member_id FK
        bigint order_id FK
        integer rating
        varchar title
        text comment
        boolean is_verified_purchase
        integer helpful_count
        varchar status
        timestamp created_at
        timestamp updated_at
    }

    wishlists {
        bigserial id PK
        uuid member_id FK
        timestamp created_at
        timestamp updated_at
    }

    wishlist_items {
        bigserial id PK
        bigint wishlist_id FK
        bigint product_id FK
        bigint variant_id FK
        timestamp created_at
    }
```

---

## Simplified ERD by Domain Context

### Product Catalog Context Only

```mermaid
erDiagram
    categories ||--o{ categories : "parent-child"
    categories ||--o{ product_categories : "has"
    products ||--o{ product_categories : "belongs to"
    products ||--o{ product_variants : "has"
    products ||--o{ product_images : "has"

    categories {
        bigserial id PK
        varchar name
        varchar slug UK
        bigint parent_id FK
        boolean is_active
    }

    products {
        bigserial id PK
        varchar name
        varchar slug UK
        decimal base_price
        varchar status
    }

    product_categories {
        bigint product_id FK
        bigint category_id FK
    }

    product_variants {
        bigserial id PK
        bigint product_id FK
        varchar sku UK
        decimal price
        jsonb attributes
    }

    product_images {
        bigserial id PK
        bigint product_id FK
        varchar image_url
        integer display_order
    }
```

### Member & Auth Context Only

```mermaid
erDiagram
    members {
        uuid id PK
        varchar email UK
        varchar password_hash
        varchar first_name
        varchar last_name
        varchar role
        varchar status
        varchar billing_street_address
        varchar billing_city
        varchar billing_postal_code
        varchar delivery_street_address
        varchar delivery_city
        varchar delivery_postal_code
    }
```

**Note:** Billing and delivery addresses are embedded as Value Objects (no separate table).

### Shopping Cart Context Only

```mermaid
erDiagram
    members ||--o| carts : "has"
    carts ||--o{ cart_items : "contains"
    products ||--o{ cart_items : "in"
    product_variants ||--o{ cart_items : "in"

    members {
        uuid id PK
        varchar email UK
    }

    carts {
        bigserial id PK
        uuid member_id FK
        varchar session_id UK
        timestamp expires_at
    }

    cart_items {
        bigserial id PK
        bigint cart_id FK
        bigint product_id FK
        bigint variant_id FK
        integer quantity
        decimal price_at_addition
    }

    products {
        bigserial id PK
        varchar name
        decimal base_price
    }

    product_variants {
        bigserial id PK
        varchar sku UK
        decimal price
    }
```

### Order Management Context Only

```mermaid
erDiagram
    members ||--o{ orders : "places"
    orders ||--o{ order_items : "contains"
    orders ||--o{ payments : "has"
    products ||--o{ order_items : "in"

    members {
        uuid id PK
        varchar email UK
    }

    orders {
        bigserial id PK
        varchar order_number UK
        uuid member_id FK
        varchar status
        decimal total
        jsonb shipping_address_snapshot
    }

    order_items {
        bigserial id PK
        bigint order_id FK
        bigint product_id FK
        bigint variant_id FK
        integer quantity
        decimal unit_price
        jsonb product_snapshot
    }

    payments {
        bigserial id PK
        bigint order_id FK
        varchar payment_method
        decimal amount
        varchar status
        varchar transaction_id UK
    }

    products {
        bigserial id PK
        varchar name
    }
```

### Inventory Context Only

```mermaid
erDiagram
    products ||--o{ inventory : "tracks"
    product_variants ||--o| inventory : "tracks"

    products {
        bigserial id PK
        varchar name
        varchar sku
    }

    product_variants {
        bigserial id PK
        bigint product_id FK
        varchar sku UK
    }

    inventory {
        bigserial id PK
        bigint product_id FK
        bigint variant_id FK
        integer quantity
        integer reserved_quantity
        integer reorder_point
    }
```

---

## Relationship Cardinality Legend

- `||--o{` : One to Many (one-to-zero-or-more)
- `||--||` : One to One
- `}o--||` : Many to One
- `}o--o{` : Many to Many
- `||--o|` : One to Zero-or-One

---

## Key Relationships Explained

### Product Catalog
1. **Category → Category** (Self-referencing)
   - Hierarchical structure for nested categories
   - One parent category can have many child categories

2. **Product ↔ Category** (Many-to-Many)
   - Through `product_categories` junction table
   - One product can belong to multiple categories

3. **Product → ProductVariant** (One-to-Many)
   - One product can have multiple variants (sizes, colors)
   - Each variant has unique SKU

4. **Product → ProductImage** (One-to-Many)
   - One product can have multiple images
   - Ordered by `display_order`

### Member Management
1. **Member Embedded Addresses** (Value Objects)
   - Billing address embedded directly in members table
   - Delivery address embedded directly in members table
   - No separate addresses table - true Value Object pattern
   - Nullable fields (members may not have addresses initially)

### Shopping Cart
1. **Member → Cart** (One-to-One)
   - Each logged-in member has one active cart
   - Guest carts use `session_id`

2. **Cart → CartItem** (One-to-Many)
   - One cart contains multiple items

3. **CartItem → Product/Variant** (Many-to-One)
   - Each cart item references one product
   - Optional variant reference

### Order Management
1. **Member → Order** (One-to-Many)
   - One member can place multiple orders
   - Guest orders have `member_id` NULL

2. **Order → OrderItem** (One-to-Many)
   - One order contains multiple items

3. **Order → Payment** (One-to-Many)
   - One order can have multiple payments (retries, partial payments)

4. **OrderItem → Product** (Many-to-One)
   - Snapshot stored in `product_snapshot` (immutable)

### Inventory
1. **Product/Variant → Inventory** (One-to-One)
   - Each product or variant has one inventory record
   - Tracks quantity and reserved stock

### Reviews (Phase 2)
1. **Product → Review** (One-to-Many)
   - One product can have many reviews

2. **Member → Review** (One-to-Many)
   - One member can write many reviews (one per product)

3. **Order → Review** (One-to-Many)
   - Verified purchase linking

### Wishlist (Phase 2)
1. **Member → Wishlist** (One-to-One)
   - One member has one wishlist

2. **Wishlist → WishlistItem** (One-to-Many)
   - One wishlist contains multiple items

---

## Database Constraints

### Primary Keys
- Most tables have `id BIGSERIAL PRIMARY KEY`
- `members` table uses `id UUID PRIMARY KEY`

### Foreign Keys
- `ON DELETE RESTRICT` (default) - prevents deletion
- `ON DELETE CASCADE` for:
  - `cart_items.cart_id`
  - Dependent entities

### Unique Constraints
- `products.slug`
- `categories.slug`
- `members.email`
- `orders.order_number`
- `product_variants.sku`
- `carts.member_id`
- `carts.session_id`
- `payments.transaction_id`

### Composite Unique Constraints
- `product_categories(product_id, category_id)`
- `cart_items(cart_id, product_id, variant_id)`
- `inventory(product_id, variant_id)`
- `reviews(product_id, member_id)`
- `wishlist_items(wishlist_id, product_id, variant_id)`

### Check Constraints
- All monetary amounts: `>= 0`
- Quantities: `> 0`
- `reviews.rating`: `BETWEEN 1 AND 5`

---

## How to View This ERD

### In GitHub/GitLab
The Mermaid diagrams will render automatically in markdown preview.

### In VS Code
Install the "Markdown Preview Mermaid Support" extension.

### Online
Copy the mermaid code blocks to: https://mermaid.live/

---

## Implementation Details

### Enums

#### MemberRole
```kotlin
enum class MemberRole {
    ADMIN,
    CUSTOMER,
    SELLER
}
```

#### MemberStatus
```kotlin
enum class MemberStatus {
    ACTIVE,
    INACTIVE,
    PENDING
}
```

### Value Objects

#### Address Value Object
**Implementation:** JPA `@Embeddable`

```kotlin
@Embeddable
data class Address(
    val streetAddress: String,
    val city: String,
    val postalCode: String
)
```

**Usage in Member Entity:**
```kotlin
@Entity
class Member(
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "streetAddress", column = Column(name = "billing_street_address")),
        AttributeOverride(name = "city", column = Column(name = "billing_city")),
        AttributeOverride(name = "postalCode", column = Column(name = "billing_postal_code"))
    )
    val billingAddress: Address?,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "streetAddress", column = Column(name = "delivery_street_address")),
        AttributeOverride(name = "city", column = Column(name = "delivery_city")),
        AttributeOverride(name = "postalCode", column = Column(name = "delivery_postal_code"))
    )
    val deliveryAddress: Address?
) : BaseEntity()
```

### Base Entity

**BaseEntity** - Mapped superclass for audit fields:
```kotlin
@MappedSuperclass
abstract class BaseEntity(
    @Column(nullable = false, name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false, name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun onPreUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
```

**Orders table:**
- Addresses stored as JSONB snapshots (immutable order history)

---

## Next Steps

1. Review the ERD - does it match your understanding?
2. Identify which context to implement first (recommendation: Product Catalog)
3. Create Flyway migration scripts based on this schema
4. Create JPA entities mapping to these tables

**Questions?**
- Any relationships unclear?
- Missing any tables or fields?
- Ready to start creating the Flyway migrations?

# Product Domain Implementation Plan

## Goal
Implement Product aggregate root with options, images, and inventory management for an e-commerce platform.

## Approach

### Domain Model
```
Product (Aggregate Root)
├── ProductOption[]      (Color, Size variations)
├── ProductImage[]       (Multiple images with order)
└── Category             (Product category - reference)

ProductOption
├── OptionType           (COLOR, SIZE, etc.)
├── OptionValue          (Red, Blue, M, L)
├── Stock                (Quantity available)
└── AdditionalPrice      (Price modifier)
```

### Core Features
1. **Product CRUD**: Create, update, soft delete
2. **Option Management**: Add/remove options with stock
3. **Image Management**: Multiple images with ordering
4. **Inventory**: Stock tracking per option
5. **Status Management**: DRAFT → ACTIVE → SOLD_OUT → DISCONTINUED

### Business Rules
- Product must have at least one option for sale
- Stock managed at option level
- Price = basePrice + option.additionalPrice
- Soft delete (retain for order history)
- Product name unique within seller

## Trade-offs

| Decision | Trade-off |
|----------|-----------|
| Stock per option | Complex but realistic for variations |
| Soft delete | Data retention vs. storage |
| Category as reference | Loose coupling vs. integrity |
| BasePrice + additionalPrice | Flexibility vs. simplicity |

## Dependencies
- Category module (optional reference)
- Member (seller) module reference
- File storage for images (external)

## Error Scenarios
- Insufficient stock → INSUFFICIENT_STOCK
- Product not found → PRODUCT_NOT_FOUND
- Option not found → PRODUCT_OPTION_NOT_FOUND
- Product not available → PRODUCT_NOT_AVAILABLE
- Duplicate product name → CONFLICT

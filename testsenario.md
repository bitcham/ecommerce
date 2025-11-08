# E-Commerce Platform Test Scenarios

This document defines test scenarios for features to be developed using TDD (Test-Driven Development) approach.

## Table of Contents
- [Authentication & Member Management](#authentication--member-management)
- [Email Verification](#email-verification)
- [Seller Management](#seller-management)
- [Product Management](#product-management)
- [Shopping Cart](#shopping-cart)
- [Order Management](#order-management)
- [Payment](#payment)
- [Reviews & Ratings](#reviews--ratings)

---

## Authentication & Member Management

### Member Registration (Customer)

**Request**
- Method: POST
- Path: /auth/register
- Headers
  ```
  Content-Type: application/json
  ```
- Body
  ```json
  {
    "email": "string",
    "password": "string",
    "firstName": "string",
    "lastName": "string",
    "phone": "string (optional)"
  }
  ```
- curl command example
  ```bash
  curl -i -X POST 'http://localhost:8080/auth/register' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "customer@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "010-1234-5678"
  }'
  ```

**Success Response**
- Status Code: 201 Created
- Body
  ```json
  {
    "success": true,
    "data": {
      "id": "uuid",
      "email": "customer@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "phone": "010-1234-5678",
      "role": "CUSTOMER",
      "status": "PENDING",
      "createdAt": "timestamp",
      "updatedAt": "timestamp"
    }
  }
  ```

**Policy**
- Email address must be unique
- Email must be in valid format
- Password must be at least 8 characters
- firstName maximum 50 characters
- lastName maximum 50 characters
- phone maximum 20 characters and optional
- Default role is CUSTOMER on registration
- Default status is PENDING on registration
- Password is encrypted with BCrypt

**Tests (Implemented)**
- [x] Should return 201 Created with member information, PENDING status, and CUSTOMER role when request is valid
- [x] Should return 201 Created even without phone number
- [x] Should return 409 Conflict when email already exists
- [x] Should return 400 Bad Request when email is invalid or missing
- [x] Should return 400 Bad Request when password validation fails (missing or less than 8 characters)
- [x] Should return 400 Bad Request when name validation fails (missing or exceeds 50 characters)
- [x] Should return 400 Bad Request when phone exceeds 20 characters
- [x] Password should be encrypted with BCrypt (Service layer test)

---

### Login

**Request**
- Method: POST
- Path: /auth/login
- Headers
  ```
  Content-Type: application/json
  ```
- Body
  ```json
  {
    "email": "string",
    "password": "string"
  }
  ```
- curl command example
  ```bash
  curl -i -X POST 'http://localhost:8080/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "customer@example.com",
    "password": "password123"
  }'
  ```

**Success Response**
- Status Code: 200 OK
- Body
  ```json
  {
    "success": true,
    "data": {
      "id": "uuid",
      "email": "customer@example.com",
      "role": "CUSTOMER",
      "accessToken": "jwt-access-token",
      "refreshToken": "jwt-refresh-token",
      "tokenType": "Bearer",
      "expiresIn": 3600000
    }
  }
  ```

**Policy**
- Email and password must match
- Member status must be ACTIVE
- Members with PENDING status cannot login
- Members with INACTIVE status cannot login
- Issues JWT access token and refresh token
- Token includes member's role information

**Tests (Implemented)**
- [x] Should return 200 OK with valid credentials and return accessToken, refreshToken, tokenType, expiresIn
- [x] Should return 401 Unauthorized when email doesn't exist
- [x] Should return 401 Unauthorized when password is incorrect
- [x] Should return 401 Unauthorized when member status is PENDING
- [x] Should return 401 Unauthorized when member status is INACTIVE
- [x] Should return 400 Bad Request when email is missing in login request
- [x] Should return 400 Bad Request when password is missing in login request
- [x] Should return 400 Bad Request when password is too short (less than 8 characters)

---

### Get My Information

**Request**
- Method: GET
- Path: /auth/me
- Headers
  ```
  Authorization: Bearer {accessToken}
  ```
- curl command example
  ```bash
  curl -i -X GET 'http://localhost:8080/auth/me' \
  -H 'Authorization: Bearer eyJhbGc...'
  ```

**Success Response**
- Status Code: 200 OK
- Body
  ```json
  {
    "success": true,
    "data": {
      "id": "uuid",
      "email": "customer@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "phone": "010-1234-5678",
      "role": "CUSTOMER",
      "status": "ACTIVE",
      "billingAddress": {
        "streetAddress": "123 Main St",
        "city": "Seoul",
        "postalCode": "12345"
      },
      "deliveryAddress": {
        "streetAddress": "456 Oak Ave",
        "city": "Seoul",
        "postalCode": "67890"
      },
      "createdAt": "timestamp",
      "updatedAt": "timestamp"
    }
  }
  ```

**Policy**
- Only authenticated users can access
- Can only retrieve own information

**Tests**
- [ ] Should return 200 OK with valid token
- [ ] Should return member information with valid token
- [ ] Should return 401 Unauthorized without token
- [ ] Should return 401 Unauthorized with invalid token
- [ ] Should return 401 Unauthorized with expired token

---

### Update My Information

**Request**
- Method: PATCH
- Path: /auth/me
- Headers
  ```
  Authorization: Bearer {accessToken}
  Content-Type: application/json
  ```
- Body
  ```json
  {
    "firstName": "string (optional)",
    "lastName": "string (optional)",
    "phone": "string (optional)",
    "billingAddress": {
      "streetAddress": "string",
      "city": "string",
      "postalCode": "string"
    },
    "deliveryAddress": {
      "streetAddress": "string",
      "city": "string",
      "postalCode": "string"
    }
  }
  ```

**Success Response**
- Status Code: 200 OK
- Body: Updated member information

**Policy**
- Only authenticated users can access
- Can only modify firstName, lastName, phone, billingAddress, deliveryAddress
- Cannot modify email, role, status
- All fields are optional (only provided fields are updated)

**Tests**
- [ ] Should return 200 OK when updating only firstName
- [ ] Should return 200 OK when updating only lastName
- [ ] Should return 200 OK when updating only phone
- [ ] Should return 200 OK when updating billingAddress
- [ ] Should return 200 OK when updating deliveryAddress
- [ ] Should return 200 OK when updating multiple fields simultaneously
- [ ] Should return 400 Bad Request when firstName exceeds 50 characters
- [ ] Should return 400 Bad Request when lastName exceeds 50 characters
- [ ] Should return 400 Bad Request when phone exceeds 20 characters
- [ ] Should return 401 Unauthorized without token

---

### Change Password

**Request**
- Method: PATCH
- Path: /auth/me/password
- Headers
  ```
  Authorization: Bearer {accessToken}
  Content-Type: application/json
  ```
- Body
  ```json
  {
    "currentPassword": "string",
    "newPassword": "string"
  }
  ```

**Success Response**
- Status Code: 204 No Content

**Policy**
- Only authenticated users can access
- Current password must match
- New password must be at least 8 characters
- New password is encrypted with BCrypt

**Tests**
- [ ] Should return 204 No Content with valid current and new passwords
- [ ] Should return 401 Unauthorized when current password doesn't match
- [ ] Should return 400 Bad Request when new password is less than 8 characters
- [ ] Should return 400 Bad Request when currentPassword is not provided
- [ ] Should return 400 Bad Request when newPassword is not provided
- [ ] Cannot login with old password after password change
- [ ] Can login with new password after password change
- [ ] Should return 401 Unauthorized without token

---

### Account Deactivation

**Request**
- Method: DELETE
- Path: /auth/me
- Headers
  ```
  Authorization: Bearer {accessToken}
  ```

**Success Response**
- Status Code: 204 No Content

**Policy**
- Only authenticated users can access
- Changes member status to INACTIVE (soft delete)
- Actual data is not deleted

**Tests**
- [ ] Should return 204 No Content with valid token
- [ ] Member status should change to INACTIVE after deactivation
- [ ] Cannot login with that email after deactivation
- [ ] Should return 401 Unauthorized without token

---

## Email Verification

### Send Email Verification Token

**Request**
- Method: POST
- Path: /auth/send-verification-email
- Headers
  ```
  Content-Type: application/json
  ```
- Body
  ```json
  {
    "email": "string"
  }
  ```

**Success Response**
- Status Code: 204 No Content

**Policy**
- Must be a registered email address
- Doesn't send to members already in ACTIVE status
- Token validity period is 24 hours
- Email includes verification link

**Tests**
- [ ] Should return 204 No Content for PENDING member email
- [ ] Should return 404 Not Found for non-existent email
- [ ] Should return 400 Bad Request for ACTIVE member email
- [ ] Verification token should be saved in database
- [ ] Token expiration time should be set to 24 hours from current time
- [ ] Email should be sent successfully
- [ ] Sent email should include verification link

---

### Email Verification

**Request**
- Method: POST
- Path: /auth/verify-email/{token}
- Path Parameter: token (UUID)

**Success Response**
- Status Code: 200 OK
- Body
  ```json
  {
    "success": true,
    "data": null,
    "message": "Email verified successfully"
  }
  ```

**Policy**
- Token must exist
- Token must not be expired
- Token must not have been used already
- Changes member status to ACTIVE on successful verification
- Marks token as used on successful verification

**Tests (Implemented)**
- [x] Should return 200 OK with valid token
- [x] Should return 404 Not Found with non-existent token
- [x] Should return 410 Gone with expired token
- [x] Should return 409 Conflict with already used token
- [x] Member status should change to ACTIVE on successful verification (Service layer test)
- [x] Token's verified field should change to true on successful verification (Service layer test)
- [x] Should return 409 Conflict for already ACTIVE member's token

---

## Seller Management

### Seller Registration

**Request**
- Method: POST
- Path: /seller/signup
- Headers
  ```
  Content-Type: application/json
  ```
- Body
  ```json
  {
    "email": "string",
    "password": "string",
    "firstName": "string",
    "lastName": "string",
    "phone": "string",
    "businessName": "string",
    "businessNumber": "string"
  }
  ```
- curl command example
  ```bash
  curl -i -X POST 'http://localhost:8080/seller/signup' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "seller1@example.com",
    "password": "seller1-password",
    "firstName": "Jane",
    "lastName": "Smith",
    "phone": "010-9876-5432",
    "businessName": "Smith Electronics",
    "businessNumber": "123-45-67890"
  }'
  ```

**Success Response**
- Status Code: 201 Created
- Body
  ```json
  {
    "success": true,
    "data": {
      "id": "uuid",
      "email": "seller1@example.com",
      "firstName": "Jane",
      "lastName": "Smith",
      "phone": "010-9876-5432",
      "role": "SELLER",
      "status": "PENDING",
      "businessName": "Smith Electronics",
      "businessNumber": "123-45-67890",
      "createdAt": "timestamp",
      "updatedAt": "timestamp"
    }
  }
  ```

**Policy**
- Email address must be unique
- Email must be in valid format
- Password must be at least 8 characters
- firstName, lastName, phone, businessName, businessNumber are required
- Role is SELLER on registration
- Status is PENDING on registration
- Business number must be unique

**Tests**
- [ ] Should return 201 Created when request is valid
- [ ] Should return seller information when request is valid
- [ ] Role should be set to SELLER
- [ ] Status should be set to PENDING
- [ ] Should return 400 Bad Request when email is not provided
- [ ] Should return 400 Bad Request when email format is invalid
- [ ] Should return 409 Conflict when email already exists
- [ ] Should return 400 Bad Request when password is less than 8 characters
- [ ] Should return 400 Bad Request when businessName is not provided
- [ ] Should return 400 Bad Request when businessNumber is not provided
- [ ] Should return 409 Conflict when businessNumber already exists

---

### Seller Login (Issue Access Token)

**Request**
- Method: POST
- Path: /seller/login
- Headers
  ```
  Content-Type: application/json
  ```
- Body
  ```json
  {
    "email": "string",
    "password": "string"
  }
  ```
- curl command example
  ```bash
  curl -i -X POST 'http://localhost:8080/seller/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "seller1@example.com",
    "password": "seller1-password"
  }'
  ```

**Success Response**
- Status Code: 200 OK
- Body
  ```json
  {
    "success": true,
    "data": {
      "id": "uuid",
      "email": "seller1@example.com",
      "role": "SELLER",
      "accessToken": "jwt-access-token",
      "refreshToken": "jwt-refresh-token",
      "tokenType": "Bearer",
      "expiresIn": 3600000
    }
  }
  ```

**Policy**
- Email and password must match
- Role must be SELLER
- Status must be ACTIVE
- Issues JWT access token and refresh token

**Tests**
- [ ] Should return 200 OK with correct email and password
- [ ] Should return accessToken when request is valid
- [ ] Should return refreshToken when request is valid
- [ ] Returned token should follow JWT format
- [ ] Token should include role as SELLER
- [ ] Should return 401 Unauthorized with non-existent email
- [ ] Should return 401 Unauthorized with incorrect password
- [ ] Should return 401 Unauthorized when CUSTOMER role member tries to login
- [ ] Should return 401 Unauthorized when PENDING status seller tries to login

---

### Get Seller Information

**Request**
- Method: GET
- Path: /seller/me
- Headers
  ```
  Authorization: Bearer {accessToken}
  ```

**Success Response**
- Status Code: 200 OK
- Body: Seller information (including businessName, businessNumber)

**Policy**
- Only SELLER role can access
- Can only retrieve own information

**Tests**
- [ ] Should return 200 OK with SELLER token
- [ ] Should return 403 Forbidden with CUSTOMER token
- [ ] Should return 401 Unauthorized without token

---

## Product Management

### Register Product

**Request**
- Method: POST
- Path: /products
- Headers
  ```
  Authorization: Bearer {accessToken}
  Content-Type: application/json
  ```
- Body
  ```json
  {
    "name": "string",
    "description": "string",
    "price": number,
    "stockQuantity": number,
    "category": "string",
    "imageUrls": ["string"]
  }
  ```

**Success Response**
- Status Code: 201 Created
- Body
  ```json
  {
    "success": true,
    "data": {
      "id": "uuid",
      "sellerId": "uuid",
      "name": "Product Name",
      "description": "Product Description",
      "price": 29.99,
      "stockQuantity": 100,
      "category": "ELECTRONICS",
      "imageUrls": ["url1", "url2"],
      "status": "ACTIVE",
      "createdAt": "timestamp",
      "updatedAt": "timestamp"
    }
  }
  ```

**Policy**
- Only SELLER role can register
- name is required and maximum 200 characters
- description maximum 2000 characters
- price must be greater than 0
- stockQuantity must be 0 or greater
- category must be one of predefined categories
- Status is ACTIVE on registration

**Tests**
- [ ] Should return 201 Created with SELLER token and valid request
- [ ] Registered product should have sellerId set to current seller's ID
- [ ] Should return 403 Forbidden with CUSTOMER token
- [ ] Should return 401 Unauthorized without token
- [ ] Should return 400 Bad Request when name is not provided
- [ ] Should return 400 Bad Request when name exceeds 200 characters
- [ ] Should return 400 Bad Request when description exceeds 2000 characters
- [ ] Should return 400 Bad Request when price is 0 or less
- [ ] Should return 400 Bad Request when stockQuantity is negative
- [ ] Should return 400 Bad Request when category is invalid

---

### Get Product List

**Request**
- Method: GET
- Path: /products
- Query Parameters
  - page: number (default: 0)
  - size: number (default: 20)
  - category: string (optional)
  - minPrice: number (optional)
  - maxPrice: number (optional)
  - keyword: string (optional)
  - sort: string (optional, values: "price,asc" | "price,desc" | "createdAt,desc")

**Success Response**
- Status Code: 200 OK
- Body
  ```json
  {
    "success": true,
    "data": {
      "content": [
        {
          "id": "uuid",
          "sellerId": "uuid",
          "name": "Product Name",
          "price": 29.99,
          "stockQuantity": 100,
          "category": "ELECTRONICS",
          "imageUrls": ["url1"],
          "status": "ACTIVE"
        }
      ],
      "page": 0,
      "size": 20,
      "totalElements": 50,
      "totalPages": 3
    }
  }
  ```

**Policy**
- Can be retrieved without authentication
- Only retrieves ACTIVE status products
- Supports pagination
- Supports filtering by category
- Supports price range filtering
- Supports keyword search (product name, description)
- Supports sorting (price, registration date)

**Tests**
- [ ] Should return 200 OK without authentication
- [ ] Default page size should be 20
- [ ] Should return only specified category products when filtered by category
- [ ] Should return only products above specified price when filtered by minPrice
- [ ] Should return only products below specified price when filtered by maxPrice
- [ ] Should return only products containing keyword in name or description
- [ ] Should be sorted by price ascending when sort=price,asc
- [ ] Should be sorted by price descending when sort=price,desc
- [ ] INACTIVE status products should not be included in list

---

### Get Product Details

**Request**
- Method: GET
- Path: /products/{productId}

**Success Response**
- Status Code: 200 OK
- Body: Product details

**Policy**
- Can be retrieved without authentication
- Only ACTIVE status products can be retrieved

**Tests**
- [ ] Should return 200 OK without authentication
- [ ] Should return 404 Not Found with non-existent product ID
- [ ] Should return 404 Not Found with INACTIVE status product ID
- [ ] Should return all product details

---

### Update Product

**Request**
- Method: PATCH
- Path: /products/{productId}
- Headers
  ```
  Authorization: Bearer {accessToken}
  Content-Type: application/json
  ```
- Body: Include only fields to update (all fields optional)

**Success Response**
- Status Code: 200 OK
- Body: Updated product information

**Policy**
- Only SELLER role can update
- Can only update products registered by self
- Cannot update sellerId

**Tests**
- [ ] Should return 200 OK when SELLER updates own product
- [ ] Should return 403 Forbidden when updating another seller's product
- [ ] Should return 403 Forbidden with CUSTOMER token
- [ ] Should return 401 Unauthorized without token
- [ ] Should return 404 Not Found with non-existent product ID
- [ ] Should return 400 Bad Request when updating price to negative

---

### Delete Product

**Request**
- Method: DELETE
- Path: /products/{productId}
- Headers
  ```
  Authorization: Bearer {accessToken}
  ```

**Success Response**
- Status Code: 204 No Content

**Policy**
- Only SELLER role can delete
- Can only delete products registered by self
- Soft delete (changes status to INACTIVE)

**Tests**
- [ ] Should return 204 No Content when SELLER deletes own product
- [ ] Product status should change to INACTIVE after deletion
- [ ] Should return 403 Forbidden when deleting another seller's product
- [ ] Should return 403 Forbidden with CUSTOMER token

---

### Get My Products

**Request**
- Method: GET
- Path: /seller/products
- Headers
  ```
  Authorization: Bearer {accessToken}
  ```
- Query Parameters
  - page: number (default: 0)
  - size: number (default: 20)

**Success Response**
- Status Code: 200 OK
- Body: Seller's product list (paginated)

**Policy**
- Only SELLER role can access
- Only retrieves products registered by self
- Retrieves both ACTIVE and INACTIVE products

**Tests**
- [ ] Should return 200 OK with SELLER token
- [ ] Should return only products registered by self
- [ ] Should include both ACTIVE and INACTIVE products
- [ ] Should return 403 Forbidden with CUSTOMER token

---

## Shopping Cart

### Add Product to Cart

**Request**
- Method: POST
- Path: /cart/items
- Headers
  ```
  Authorization: Bearer {accessToken}
  Content-Type: application/json
  ```
- Body
  ```json
  {
    "productId": "uuid",
    "quantity": number
  }
  ```

**Success Response**
- Status Code: 201 Created
- Body
  ```json
  {
    "success": true,
    "data": {
      "id": "uuid",
      "productId": "uuid",
      "productName": "Product Name",
      "price": 29.99,
      "quantity": 2,
      "subtotal": 59.98
    }
  }
  ```

**Policy**
- Only CUSTOMER role allowed
- Only authenticated users can access
- quantity must be 1 or greater
- Product stock must be sufficient
- If product already in cart, increases quantity
- Only ACTIVE status products can be added

**Tests**
- [ ] Should return 201 Created with CUSTOMER token and valid request
- [ ] Should increase quantity when adding existing product in cart
- [ ] Should return 400 Bad Request when quantity is 0 or less
- [ ] Should return 400 Bad Request when stock is insufficient
- [ ] Should return 404 Not Found with non-existent product ID
- [ ] Should return 400 Bad Request when adding INACTIVE status product
- [ ] Should return 403 Forbidden with SELLER token
- [ ] Should return 401 Unauthorized without token

---

### Get Cart

**Request**
- Method: GET
- Path: /cart
- Headers
  ```
  Authorization: Bearer {accessToken}
  ```

**Success Response**
- Status Code: 200 OK
- Body
  ```json
  {
    "success": true,
    "data": {
      "items": [
        {
          "id": "uuid",
          "productId": "uuid",
          "productName": "Product Name",
          "price": 29.99,
          "quantity": 2,
          "subtotal": 59.98
        }
      ],
      "totalItems": 3,
      "totalPrice": 89.97
    }
  }
  ```

**Policy**
- Only CUSTOMER role allowed
- Can only retrieve own cart

**Tests**
- [ ] Should return 200 OK with CUSTOMER token
- [ ] Should return empty array when cart is empty
- [ ] totalPrice should be calculated correctly
- [ ] Should return 403 Forbidden with SELLER token

---

### Update Cart Item Quantity

**Request**
- Method: PATCH
- Path: /cart/items/{cartItemId}
- Headers
  ```
  Authorization: Bearer {accessToken}
  Content-Type: application/json
  ```
- Body
  ```json
  {
    "quantity": number
  }
  ```

**Success Response**
- Status Code: 200 OK
- Body: Updated cart item

**Policy**
- Can only update own cart items
- quantity must be 1 or greater
- Product stock must be sufficient

**Tests**
- [ ] Should return 200 OK when request is valid
- [ ] Should return 400 Bad Request when quantity is 0 or less
- [ ] Should return 400 Bad Request when stock is insufficient
- [ ] Should return 403 Forbidden when updating another user's cart item

---

### Delete Cart Item

**Request**
- Method: DELETE
- Path: /cart/items/{cartItemId}
- Headers
  ```
  Authorization: Bearer {accessToken}
  ```

**Success Response**
- Status Code: 204 No Content

**Policy**
- Can only delete own cart items

**Tests**
- [ ] Should return 204 No Content when request is valid
- [ ] Should return 404 Not Found with non-existent item ID
- [ ] Should return 403 Forbidden when deleting another user's cart item

---

### Clear Cart

**Request**
- Method: DELETE
- Path: /cart
- Headers
  ```
  Authorization: Bearer {accessToken}
  ```

**Success Response**
- Status Code: 204 No Content

**Policy**
- Can only clear own cart
- All cart items are deleted

**Tests**
- [ ] Should return 204 No Content when request is valid
- [ ] Should return empty array when retrieving cart after clearing

---

## Order Management

### Create Order

**Request**
- Method: POST
- Path: /orders
- Headers
  ```
  Authorization: Bearer {accessToken}
  Content-Type: application/json
  ```
- Body
  ```json
  {
    "items": [
      {
        "productId": "uuid",
        "quantity": number
      }
    ],
    "deliveryAddress": {
      "streetAddress": "string",
      "city": "string",
      "postalCode": "string"
    },
    "paymentMethod": "CARD" | "BANK_TRANSFER"
  }
  ```

**Success Response**
- Status Code: 201 Created
- Body
  ```json
  {
    "success": true,
    "data": {
      "orderId": "uuid",
      "orderNumber": "ORD-20250103-00001",
      "status": "PENDING_PAYMENT",
      "items": [
        {
          "productId": "uuid",
          "productName": "Product Name",
          "price": 29.99,
          "quantity": 2,
          "subtotal": 59.98
        }
      ],
      "totalAmount": 59.98,
      "deliveryAddress": {
        "streetAddress": "456 Oak Ave",
        "city": "Seoul",
        "postalCode": "67890"
      },
      "paymentMethod": "CARD",
      "createdAt": "timestamp"
    }
  }
  ```

**Policy**
- Only CUSTOMER role allowed
- items must have at least 1 item
- All products must be in ACTIVE status
- All products must have sufficient stock
- Decreases stock on order creation
- Order number is auto-generated (ORD-YYYYMMDD-xxxxx)
- Initial status is PENDING_PAYMENT

**Tests**
- [ ] Should return 201 Created with CUSTOMER token and valid request
- [ ] Order number should be auto-generated
- [ ] Order status should be set to PENDING_PAYMENT
- [ ] Product stock should decrease on order creation
- [ ] Should return 400 Bad Request when items is empty
- [ ] Should return 400 Bad Request when stock is insufficient
- [ ] Should return 400 Bad Request when INACTIVE product is included
- [ ] Should return 400 Bad Request when deliveryAddress is not provided
- [ ] Should return 403 Forbidden with SELLER token

---

### Get Order List (Customer)

**Request**
- Method: GET
- Path: /orders
- Headers
  ```
  Authorization: Bearer {accessToken}
  ```
- Query Parameters
  - page: number (default: 0)
  - size: number (default: 20)
  - status: string (optional)

**Success Response**
- Status Code: 200 OK
- Body: Paginated order list

**Policy**
- Only CUSTOMER role allowed
- Can only retrieve own orders
- Supports filtering by status

**Tests**
- [ ] Should return 200 OK with CUSTOMER token
- [ ] Should return only own orders
- [ ] Should return only orders with specified status when filtered
- [ ] Pagination should work correctly
- [ ] Should return 403 Forbidden with SELLER token

---

### Get Order Details

**Request**
- Method: GET
- Path: /orders/{orderId}
- Headers
  ```
  Authorization: Bearer {accessToken}
  ```

**Success Response**
- Status Code: 200 OK
- Body: Order details

**Policy**
- Can only retrieve own orders (CUSTOMER)
- Can only retrieve orders containing own products (SELLER)

**Tests**
- [ ] Should return 200 OK when CUSTOMER retrieves own order
- [ ] Should return 200 OK when SELLER retrieves order containing own products
- [ ] Should return 403 Forbidden when retrieving another customer's order
- [ ] Should return 404 Not Found with non-existent order ID

---

### Cancel Order

**Request**
- Method: POST
- Path: /orders/{orderId}/cancel
- Headers
  ```
  Authorization: Bearer {accessToken}
  Content-Type: application/json
  ```
- Body
  ```json
  {
    "reason": "string"
  }
  ```

**Success Response**
- Status Code: 200 OK
- Body: Cancelled order information

**Policy**
- Only CUSTOMER allowed
- Can only cancel own orders
- Can only cancel in PENDING_PAYMENT or PAID status
- Cannot cancel in SHIPPED, DELIVERED, CANCELLED status
- Restores stock on cancellation
- Cancellation reason is required

**Tests**
- [ ] Should return 200 OK when cancelling PENDING_PAYMENT status order
- [ ] Should return 200 OK when cancelling PAID status order
- [ ] Order status should change to CANCELLED after cancellation
- [ ] Product stock should be restored on cancellation
- [ ] Should return 400 Bad Request when cancelling SHIPPED status order
- [ ] Should return 400 Bad Request when cancelling DELIVERED status order
- [ ] Should return 400 Bad Request when cancelling CANCELLED status order
- [ ] Should return 400 Bad Request when reason is not provided
- [ ] Should return 403 Forbidden when cancelling another customer's order

---

### Get Seller Order List

**Request**
- Method: GET
- Path: /seller/orders
- Headers
  ```
  Authorization: Bearer {accessToken}
  ```
- Query Parameters
  - page: number (default: 0)
  - size: number (default: 20)
  - status: string (optional)

**Success Response**
- Status Code: 200 OK
- Body: Order list containing own products

**Policy**
- Only SELLER role allowed
- Only retrieves orders containing own products

**Tests**
- [ ] Should return 200 OK with SELLER token
- [ ] Should return only orders containing own products
- [ ] Should return 403 Forbidden with CUSTOMER token

---

### Update Order Status (Seller)

**Request**
- Method: PATCH
- Path: /seller/orders/{orderId}/status
- Headers
  ```
  Authorization: Bearer {accessToken}
  Content-Type: application/json
  ```
- Body
  ```json
  {
    "status": "PREPARING" | "SHIPPED" | "DELIVERED"
  }
  ```

**Success Response**
- Status Code: 200 OK
- Body: Updated order information

**Policy**
- Only SELLER role allowed
- Can only update orders containing own products
- Status transition rules:
  - PAID → PREPARING
  - PREPARING → SHIPPED
  - SHIPPED → DELIVERED

**Tests**
- [ ] Should return 200 OK when changing PAID to PREPARING
- [ ] Should return 200 OK when changing PREPARING to SHIPPED
- [ ] Should return 200 OK when changing SHIPPED to DELIVERED
- [ ] Should return 400 Bad Request for invalid status transition
- [ ] Should return 403 Forbidden when updating another seller's order

---

## Payment

### Process Payment

**Request**
- Method: POST
- Path: /payments
- Headers
  ```
  Authorization: Bearer {accessToken}
  Content-Type: application/json
  ```
- Body
  ```json
  {
    "orderId": "uuid",
    "paymentMethod": "CARD" | "BANK_TRANSFER",
    "cardNumber": "string (if CARD)",
    "cardExpiry": "string (if CARD)",
    "cardCvv": "string (if CARD)"
  }
  ```

**Success Response**
- Status Code: 200 OK
- Body
  ```json
  {
    "success": true,
    "data": {
      "paymentId": "uuid",
      "orderId": "uuid",
      "amount": 59.98,
      "paymentMethod": "CARD",
      "status": "COMPLETED",
      "transactionId": "TXN-20250103-00001",
      "paidAt": "timestamp"
    }
  }
  ```

**Policy**
- Only CUSTOMER role allowed
- Can only pay for own orders
- Order status must be PENDING_PAYMENT
- Changes order status to PAID on successful payment
- Order status remains unchanged on payment failure

**Tests**
- [ ] Should return 200 OK when request is valid
- [ ] Order status should change to PAID on successful payment
- [ ] Payment record should be created on successful payment
- [ ] Should return 403 Forbidden when paying for another customer's order
- [ ] Should return 400 Bad Request when paying for already paid order
- [ ] Should return 400 Bad Request when paying for CANCELLED order
- [ ] Should return 400 Bad Request when paymentMethod is CARD but card info is missing

---

### Get Payment Details

**Request**
- Method: GET
- Path: /payments/{paymentId}
- Headers
  ```
  Authorization: Bearer {accessToken}
  ```

**Success Response**
- Status Code: 200 OK
- Body: Payment details

**Policy**
- Can only retrieve own payment records

**Tests**
- [ ] Should return 200 OK when request is valid
- [ ] Should return 403 Forbidden when retrieving another customer's payment
- [ ] Should return 404 Not Found with non-existent payment ID

---

## Reviews & Ratings

### Write Review

**Request**
- Method: POST
- Path: /products/{productId}/reviews
- Headers
  ```
  Authorization: Bearer {accessToken}
  Content-Type: application/json
  ```
- Body
  ```json
  {
    "rating": number,
    "comment": "string",
    "imageUrls": ["string"]
  }
  ```

**Success Response**
- Status Code: 201 Created
- Body
  ```json
  {
    "success": true,
    "data": {
      "id": "uuid",
      "productId": "uuid",
      "customerId": "uuid",
      "customerName": "John Doe",
      "rating": 5,
      "comment": "Great product!",
      "imageUrls": ["url1"],
      "createdAt": "timestamp"
    }
  }
  ```

**Policy**
- Only CUSTOMER role allowed
- Only customers who purchased the product can write reviews
- Order status must be DELIVERED
- rating must be integer between 1-5
- comment maximum 1000 characters
- Can write only one review per product

**Tests**
- [ ] Should return 201 Created when writing review for purchased product
- [ ] Should return 403 Forbidden when writing review for unpurchased product
- [ ] Should return 403 Forbidden for non-DELIVERED order product
- [ ] Should return 400 Bad Request when rating is less than 1
- [ ] Should return 400 Bad Request when rating is greater than 5
- [ ] Should return 400 Bad Request when comment exceeds 1000 characters
- [ ] Should return 409 Conflict when writing another review for same product
- [ ] Should return 403 Forbidden with SELLER token

---

### Get Review List

**Request**
- Method: GET
- Path: /products/{productId}/reviews
- Query Parameters
  - page: number (default: 0)
  - size: number (default: 20)
  - sort: string (optional, values: "rating,desc" | "createdAt,desc")

**Success Response**
- Status Code: 200 OK
- Body: Paginated review list

**Policy**
- Can be retrieved without authentication
- Supports sorting by latest or rating

**Tests**
- [ ] Should return 200 OK without authentication
- [ ] Should be sorted by rating descending when sort=rating,desc
- [ ] Should be sorted by latest when sort=createdAt,desc
- [ ] Pagination should work correctly

---

### Update Review

**Request**
- Method: PATCH
- Path: /reviews/{reviewId}
- Headers
  ```
  Authorization: Bearer {accessToken}
  Content-Type: application/json
  ```
- Body
  ```json
  {
    "rating": number (optional),
    "comment": "string (optional)",
    "imageUrls": ["string"] (optional)
  }
  ```

**Success Response**
- Status Code: 200 OK
- Body: Updated review information

**Policy**
- Can only update own reviews
- All fields are optional

**Tests**
- [ ] Should return 200 OK when updating own review
- [ ] Should return 403 Forbidden when updating another customer's review
- [ ] Should return 400 Bad Request when rating is less than 1
- [ ] Should return 400 Bad Request when rating is greater than 5

---

### Delete Review

**Request**
- Method: DELETE
- Path: /reviews/{reviewId}
- Headers
  ```
  Authorization: Bearer {accessToken}
  ```

**Success Response**
- Status Code: 204 No Content

**Policy**
- Can only delete own reviews

**Tests**
- [ ] Should return 204 No Content when deleting own review
- [ ] Should return 403 Forbidden when deleting another customer's review
- [ ] Should return 404 Not Found with non-existent review ID

---

## Admin Features

### Get All Members

**Request**
- Method: GET
- Path: /admin/members
- Headers
  ```
  Authorization: Bearer {accessToken}
  ```
- Query Parameters
  - page: number (default: 0)
  - size: number (default: 20)
  - role: string (optional)
  - status: string (optional)

**Success Response**
- Status Code: 200 OK
- Body: Paginated member list

**Policy**
- Only ADMIN role can access
- Supports filtering by role and status

**Tests**
- [ ] Should return 200 OK with ADMIN token
- [ ] Should return 403 Forbidden with CUSTOMER token
- [ ] Should return 403 Forbidden with SELLER token
- [ ] Should return only members with specified role when filtered
- [ ] Should return only members with specified status when filtered

---

### Update Member Status (Admin)

**Request**
- Method: PATCH
- Path: /admin/members/{memberId}/status
- Headers
  ```
  Authorization: Bearer {accessToken}
  Content-Type: application/json
  ```
- Body
  ```json
  {
    "status": "ACTIVE" | "INACTIVE"
  }
  ```

**Success Response**
- Status Code: 200 OK
- Body: Updated member information

**Policy**
- Only ADMIN role allowed
- Can freely change between PENDING, ACTIVE, INACTIVE statuses

**Tests**
- [ ] Should return 200 OK with ADMIN token
- [ ] Member status should be updated
- [ ] Should return 403 Forbidden with CUSTOMER token
- [ ] Should return 403 Forbidden with SELLER token

---

### Get All Products (Admin)

**Request**
- Method: GET
- Path: /admin/products
- Headers
  ```
  Authorization: Bearer {accessToken}
  ```
- Query Parameters
  - page: number (default: 0)
  - size: number (default: 20)
  - status: string (optional)

**Success Response**
- Status Code: 200 OK
- Body: Paginated product list (both ACTIVE and INACTIVE)

**Policy**
- Only ADMIN role can access
- Can retrieve both ACTIVE and INACTIVE products

**Tests**
- [ ] Should return 200 OK with ADMIN token
- [ ] Should return both ACTIVE and INACTIVE products
- [ ] Should return 403 Forbidden with CUSTOMER token

---

### Update Product Status (Admin)

**Request**
- Method: PATCH
- Path: /admin/products/{productId}/status
- Headers
  ```
  Authorization: Bearer {accessToken}
  Content-Type: application/json
  ```
- Body
  ```json
  {
    "status": "ACTIVE" | "INACTIVE"
  }
  ```

**Success Response**
- Status Code: 200 OK
- Body: Updated product information

**Policy**
- Only ADMIN role allowed
- Can change inappropriate products to INACTIVE

**Tests**
- [ ] Should return 200 OK with ADMIN token
- [ ] Product status should be updated
- [ ] Should return 403 Forbidden with CUSTOMER token
- [ ] Should return 403 Forbidden with SELLER token

---

## Statistics & Dashboard

### Seller Dashboard

**Request**
- Method: GET
- Path: /seller/dashboard
- Headers
  ```
  Authorization: Bearer {accessToken}
  ```

**Success Response**
- Status Code: 200 OK
- Body
  ```json
  {
    "success": true,
    "data": {
      "totalProducts": 25,
      "activeProducts": 20,
      "totalOrders": 150,
      "pendingOrders": 5,
      "totalRevenue": 15000.50,
      "monthlyRevenue": 2500.00
    }
  }
  ```

**Policy**
- Only SELLER role can access
- Only retrieves own statistics

**Tests**
- [ ] Should return 200 OK with SELLER token
- [ ] Statistics should be calculated correctly
- [ ] Should return 403 Forbidden with CUSTOMER token

---

### Admin Dashboard

**Request**
- Method: GET
- Path: /admin/dashboard
- Headers
  ```
  Authorization: Bearer {accessToken}
  ```

**Success Response**
- Status Code: 200 OK
- Body
  ```json
  {
    "success": true,
    "data": {
      "totalMembers": 1000,
      "activeMembers": 800,
      "totalSellers": 50,
      "totalProducts": 500,
      "totalOrders": 2000,
      "totalRevenue": 100000.00
    }
  }
  ```

**Policy**
- Only ADMIN role can access
- Retrieves entire platform statistics

**Tests**
- [ ] Should return 200 OK with ADMIN token
- [ ] Statistics should be calculated correctly
- [ ] Should return 403 Forbidden with CUSTOMER token
- [ ] Should return 403 Forbidden with SELLER token

---

## Search

### Integrated Search

**Request**
- Method: GET
- Path: /search
- Query Parameters
  - q: string (required, search keyword)
  - type: string (optional, values: "products" | "sellers")
  - page: number (default: 0)
  - size: number (default: 20)

**Success Response**
- Status Code: 200 OK
- Body: Search results (products or sellers)

**Policy**
- Can be accessed without authentication
- Searches by product name, description, seller name
- Minimum 2 characters required for search keyword

**Tests**
- [ ] Should return 200 OK without authentication
- [ ] Should return 400 Bad Request when search keyword is less than 2 characters
- [ ] Should return product search results when type=products
- [ ] Should return seller search results when type=sellers
- [ ] Should search both products and sellers when type is not specified

---

## Security & Authorization

### Refresh Access Token with Refresh Token

**Request**
- Method: POST
- Path: /auth/refresh
- Headers
  ```
  Content-Type: application/json
  ```
- Body
  ```json
  {
    "refreshToken": "string"
  }
  ```

**Success Response**
- Status Code: 200 OK
- Body
  ```json
  {
    "success": true,
    "data": {
      "accessToken": "new-jwt-access-token",
      "refreshToken": "new-jwt-refresh-token",
      "tokenType": "Bearer",
      "expiresIn": 3600000
    }
  }
  ```

**Policy**
- Refresh Token must be valid
- Refresh Token must not be expired
- Issues new Access Token and Refresh Token

**Tests**
- [ ] Should return 200 OK with valid Refresh Token
- [ ] Should issue new Access Token
- [ ] Should issue new Refresh Token
- [ ] Should return 401 Unauthorized with invalid Refresh Token
- [ ] Should return 401 Unauthorized with expired Refresh Token

---

### Logout

**Request**
- Method: POST
- Path: /auth/logout
- Headers
  ```
  Authorization: Bearer {accessToken}
  ```

**Success Response**
- Status Code: 204 No Content

**Policy**
- Only authenticated users can access
- Invalidates Refresh Token (adds to blacklist)

**Tests**
- [ ] Should return 204 No Content with valid token
- [ ] Should return 401 Unauthorized when trying to refresh with logged out Refresh Token
- [ ] Should return 401 Unauthorized without token

---

## Common Validations

### Input Validation

Common validations applied to all APIs:

**Tests**
- [ ] Should return 400 Bad Request with invalid JSON format
- [ ] Should return 400 Bad Request when required fields are missing
- [ ] Should return 400 Bad Request with incorrect field types
- [ ] Should return 415 Unsupported Media Type with incorrect Content-Type

---

## Notes

Recommended implementation order for TDD approach:

1. **Authentication & Member Management** (Basic features) - ✅ **Complete**
   - Member Registration: 7 tests implemented
   - Login: 8 tests implemented
   - Get My Information: Not started
   - Update My Information: Not started
   - Change Password: Not started
   - Account Deactivation: Not started

2. **Email Verification** - ✅ **Complete** (Automatic email sending on registration)
   - Email Verification: 11 tests implemented (5 Controller + 6 Service)
   - Send Email Verification Token: Not implemented (emails sent automatically on registration)

3. **Seller Management** - Not started
4. **Product Management** - Not started
5. **Shopping Cart** - Not started
6. **Order Management** - Not started
7. **Payment** - Not started
8. **Reviews & Ratings** - Not started
9. **Admin Features** - Not started
10. **Statistics & Dashboard** - Not started
11. **Search** - Not started
12. **Security & Authorization (Advanced)** - Not started

**Total Tests Implemented: 30 tests (All Passing)**
- AuthControllerTest: 14 tests (Register: 6, Login: 8)
- EmailVerificationControllerTest: 5 tests
- AuthServiceTest: 4 tests
- EmailVerificationServiceTest: 6 tests
- MemberServiceTest: 1 test (placeholder for future)

Development cycle for each feature using TDD:
1. Write test (Red)
2. Minimal implementation (Green)
3. Refactoring (Refactor)
4. Move to next test
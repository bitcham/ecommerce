package platform.ecommerce.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Centralized error codes for the application.
 * Format: {DOMAIN}_{ERROR_TYPE} with numeric code.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common (1xxx)
    INVALID_INPUT(1001, HttpStatus.BAD_REQUEST, "Invalid input"),
    RESOURCE_NOT_FOUND(1002, HttpStatus.NOT_FOUND, "Resource not found"),
    UNAUTHORIZED(1003, HttpStatus.UNAUTHORIZED, "Unauthorized access"),
    FORBIDDEN(1004, HttpStatus.FORBIDDEN, "Access forbidden"),
    METHOD_NOT_ALLOWED(1005, HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed"),
    CONFLICT(1006, HttpStatus.CONFLICT, "Resource conflict"),

    // Auth (2xxx)
    INVALID_CREDENTIALS(2001, HttpStatus.UNAUTHORIZED, "Invalid email or password"),
    TOKEN_EXPIRED(2002, HttpStatus.UNAUTHORIZED, "Token has expired"),
    TOKEN_INVALID(2003, HttpStatus.UNAUTHORIZED, "Invalid token"),
    EMAIL_NOT_VERIFIED(2004, HttpStatus.FORBIDDEN, "Email not verified"),
    EMAIL_ALREADY_EXISTS(2005, HttpStatus.CONFLICT, "Email already registered"),
    REFRESH_TOKEN_NOT_FOUND(2006, HttpStatus.UNAUTHORIZED, "Refresh token not found"),
    REFRESH_TOKEN_EXPIRED(2007, HttpStatus.UNAUTHORIZED, "Refresh token has expired"),

    // Member (3xxx)
    MEMBER_NOT_FOUND(3001, HttpStatus.NOT_FOUND, "Member not found"),
    MEMBER_SUSPENDED(3002, HttpStatus.FORBIDDEN, "Member account is suspended"),
    MEMBER_ALREADY_WITHDRAWN(3003, HttpStatus.BAD_REQUEST, "Member already withdrawn"),
    MEMBER_ADDRESS_NOT_FOUND(3004, HttpStatus.NOT_FOUND, "Member address not found"),
    MEMBER_ADDRESS_LIMIT_EXCEEDED(3005, HttpStatus.BAD_REQUEST, "Address limit exceeded (max 10)"),
    MEMBER_EMAIL_DUPLICATED(3006, HttpStatus.CONFLICT, "Email already registered"),

    // Seller (34xx)
    SELLER_NOT_FOUND(3401, HttpStatus.NOT_FOUND, "Seller not found"),
    SELLER_NOT_APPROVED(3402, HttpStatus.FORBIDDEN, "Seller not approved"),
    SELLER_ALREADY_EXISTS(3403, HttpStatus.CONFLICT, "Seller already registered"),
    BUSINESS_NUMBER_ALREADY_EXISTS(3404, HttpStatus.CONFLICT, "Business number already exists"),

    // Product (4xxx)
    PRODUCT_NOT_FOUND(4001, HttpStatus.NOT_FOUND, "Product not found"),
    PRODUCT_SOLD_OUT(4002, HttpStatus.BAD_REQUEST, "Product is sold out"),
    INSUFFICIENT_STOCK(4003, HttpStatus.BAD_REQUEST, "Insufficient stock"),
    PRODUCT_OPTION_NOT_FOUND(4004, HttpStatus.NOT_FOUND, "Product option not found"),
    PRODUCT_NOT_AVAILABLE(4005, HttpStatus.BAD_REQUEST, "Product is not available"),
    PRODUCT_IMAGE_NOT_FOUND(4006, HttpStatus.NOT_FOUND, "Product image not found"),

    // Category (45xx)
    CATEGORY_NOT_FOUND(4501, HttpStatus.NOT_FOUND, "Category not found"),
    CATEGORY_HAS_CHILDREN(4502, HttpStatus.BAD_REQUEST, "Category has child categories"),
    CATEGORY_HAS_PRODUCTS(4503, HttpStatus.BAD_REQUEST, "Category has products"),

    // Order (5xxx)
    ORDER_NOT_FOUND(5001, HttpStatus.NOT_FOUND, "Order not found"),
    ORDER_CANNOT_CANCEL(5002, HttpStatus.BAD_REQUEST, "Order cannot be cancelled"),
    ORDER_ALREADY_PAID(5003, HttpStatus.BAD_REQUEST, "Order is already paid"),
    ORDER_NOT_PAID(5004, HttpStatus.BAD_REQUEST, "Order is not paid yet"),
    ORDER_ITEM_NOT_FOUND(5005, HttpStatus.NOT_FOUND, "Order item not found"),
    ORDER_STATUS_INVALID(5006, HttpStatus.BAD_REQUEST, "Invalid order status transition"),

    // Payment (6xxx)
    PAYMENT_FAILED(6001, HttpStatus.BAD_REQUEST, "Payment failed"),
    PAYMENT_ALREADY_COMPLETED(6002, HttpStatus.BAD_REQUEST, "Payment already completed"),
    PAYMENT_NOT_FOUND(6003, HttpStatus.NOT_FOUND, "Payment not found"),
    PAYMENT_AMOUNT_MISMATCH(6004, HttpStatus.BAD_REQUEST, "Payment amount mismatch"),
    REFUND_FAILED(6005, HttpStatus.BAD_REQUEST, "Refund failed"),
    PAYMENT_ALREADY_PROCESSED(6006, HttpStatus.BAD_REQUEST, "Payment already processed"),
    PAYMENT_CANNOT_CANCEL(6007, HttpStatus.BAD_REQUEST, "Payment cannot be cancelled"),

    // Cart (7xxx)
    CART_NOT_FOUND(7001, HttpStatus.NOT_FOUND, "Cart not found"),
    CART_ITEM_NOT_FOUND(7002, HttpStatus.NOT_FOUND, "Cart item not found"),
    CART_EMPTY(7003, HttpStatus.BAD_REQUEST, "Cart is empty"),
    CART_ITEM_ALREADY_EXISTS(7004, HttpStatus.CONFLICT, "Item already in cart"),

    // Coupon (8xxx)
    COUPON_NOT_FOUND(8001, HttpStatus.NOT_FOUND, "Coupon not found"),
    COUPON_EXPIRED(8002, HttpStatus.BAD_REQUEST, "Coupon has expired"),
    COUPON_ALREADY_USED(8003, HttpStatus.BAD_REQUEST, "Coupon already used"),
    COUPON_NOT_APPLICABLE(8004, HttpStatus.BAD_REQUEST, "Coupon not applicable"),
    COUPON_LIMIT_EXCEEDED(8005, HttpStatus.BAD_REQUEST, "Coupon issuance limit exceeded"),

    // Review (85xx)
    REVIEW_NOT_FOUND(8501, HttpStatus.NOT_FOUND, "Review not found"),
    REVIEW_ALREADY_EXISTS(8502, HttpStatus.CONFLICT, "Review already exists for this order item"),
    REVIEW_NOT_ALLOWED(8503, HttpStatus.FORBIDDEN, "Review not allowed"),

    // Delivery (86xx)
    DELIVERY_NOT_FOUND(8601, HttpStatus.NOT_FOUND, "Delivery not found"),

    // Wishlist (87xx)
    WISHLIST_NOT_FOUND(8701, HttpStatus.NOT_FOUND, "Wishlist item not found"),
    WISHLIST_ALREADY_EXISTS(8702, HttpStatus.CONFLICT, "Product already in wishlist"),

    // System (9xxx)
    INTERNAL_ERROR(9001, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    SERVICE_UNAVAILABLE(9002, HttpStatus.SERVICE_UNAVAILABLE, "Service temporarily unavailable"),
    EXTERNAL_API_ERROR(9003, HttpStatus.BAD_GATEWAY, "External service error"),
    RATE_LIMIT_EXCEEDED(9004, HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded"),
    FILE_UPLOAD_FAILED(9005, HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}

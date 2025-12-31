package platform.ecommerce.dto.request.coupon;

import jakarta.validation.constraints.*;
import lombok.Builder;
import platform.ecommerce.domain.coupon.CouponType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for creating a coupon.
 */
@Builder
public record CouponCreateRequest(

        @NotBlank(message = "Coupon code is required")
        @Size(max = 50)
        @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Code must be alphanumeric")
        String code,

        @NotBlank(message = "Coupon name is required")
        @Size(max = 100)
        String name,

        @NotNull(message = "Coupon type is required")
        CouponType type,

        @NotNull(message = "Discount value is required")
        @DecimalMin(value = "0.01", message = "Discount value must be positive")
        BigDecimal discountValue,

        @DecimalMin(value = "0", message = "Minimum order cannot be negative")
        BigDecimal minimumOrder,

        @DecimalMin(value = "0", message = "Maximum discount cannot be negative")
        BigDecimal maximumDiscount,

        @NotNull(message = "Valid from date is required")
        LocalDateTime validFrom,

        @NotNull(message = "Valid to date is required")
        LocalDateTime validTo,

        @Min(value = 1, message = "Total quantity must be at least 1")
        int totalQuantity
) {
}

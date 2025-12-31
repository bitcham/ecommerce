package platform.ecommerce.domain.coupon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import platform.ecommerce.exception.InvalidStateException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Coupon aggregate.
 */
class CouponTest {

    private Coupon percentageCoupon;
    private Coupon fixedCoupon;

    @BeforeEach
    void setUp() {
        percentageCoupon = Coupon.builder()
                .code("SAVE10")
                .name("10% Off")
                .type(CouponType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(10))
                .minimumOrder(BigDecimal.valueOf(10000))
                .maximumDiscount(BigDecimal.valueOf(5000))
                .validFrom(LocalDateTime.now().minusDays(1))
                .validTo(LocalDateTime.now().plusDays(30))
                .totalQuantity(100)
                .build();
        ReflectionTestUtils.setField(percentageCoupon, "id", 1L);

        fixedCoupon = Coupon.builder()
                .code("FLAT5000")
                .name("₩5000 Off")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(BigDecimal.valueOf(5000))
                .minimumOrder(BigDecimal.valueOf(20000))
                .validFrom(LocalDateTime.now().minusDays(1))
                .validTo(LocalDateTime.now().plusDays(30))
                .totalQuantity(50)
                .build();
        ReflectionTestUtils.setField(fixedCoupon, "id", 2L);
    }

    @Nested
    @DisplayName("Coupon Creation")
    class CouponCreation {

        @Test
        @DisplayName("should create percentage coupon")
        void createPercentageCoupon() {
            // when
            Coupon coupon = Coupon.builder()
                    .code("test10")
                    .name("Test 10%")
                    .type(CouponType.PERCENTAGE)
                    .discountValue(BigDecimal.valueOf(10))
                    .validFrom(LocalDateTime.now())
                    .validTo(LocalDateTime.now().plusDays(7))
                    .totalQuantity(100)
                    .build();

            // then
            assertThat(coupon.getCode()).isEqualTo("TEST10"); // Uppercase
            assertThat(coupon.getType()).isEqualTo(CouponType.PERCENTAGE);
            assertThat(coupon.isActive()).isTrue();
            assertThat(coupon.getUsedQuantity()).isZero();
        }

        @Test
        @DisplayName("should create fixed amount coupon")
        void createFixedAmountCoupon() {
            // when
            Coupon coupon = Coupon.builder()
                    .code("fixed")
                    .name("Fixed Discount")
                    .type(CouponType.FIXED_AMOUNT)
                    .discountValue(BigDecimal.valueOf(3000))
                    .validFrom(LocalDateTime.now())
                    .validTo(LocalDateTime.now().plusDays(7))
                    .totalQuantity(50)
                    .build();

            // then
            assertThat(coupon.getType()).isEqualTo(CouponType.FIXED_AMOUNT);
        }

        @Test
        @DisplayName("should throw exception for negative discount value")
        void throwOnNegativeDiscount() {
            // when & then
            assertThatThrownBy(() -> Coupon.builder()
                    .code("bad")
                    .name("Bad Coupon")
                    .type(CouponType.FIXED_AMOUNT)
                    .discountValue(BigDecimal.valueOf(-100))
                    .validFrom(LocalDateTime.now())
                    .validTo(LocalDateTime.now().plusDays(1))
                    .totalQuantity(10)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw exception for percentage over 100")
        void throwOnPercentageOver100() {
            // when & then
            assertThatThrownBy(() -> Coupon.builder()
                    .code("over")
                    .name("Over 100%")
                    .type(CouponType.PERCENTAGE)
                    .discountValue(BigDecimal.valueOf(150))
                    .validFrom(LocalDateTime.now())
                    .validTo(LocalDateTime.now().plusDays(1))
                    .totalQuantity(10)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw exception for invalid date range")
        void throwOnInvalidDateRange() {
            // when & then
            assertThatThrownBy(() -> Coupon.builder()
                    .code("bad-date")
                    .name("Bad Date")
                    .type(CouponType.FIXED_AMOUNT)
                    .discountValue(BigDecimal.valueOf(1000))
                    .validFrom(LocalDateTime.now().plusDays(10))
                    .validTo(LocalDateTime.now())
                    .totalQuantity(10)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Discount Calculation")
    class DiscountCalculation {

        @Test
        @DisplayName("should calculate percentage discount correctly")
        void calculatePercentageDiscount() {
            // given
            BigDecimal orderAmount = BigDecimal.valueOf(30000);

            // when
            BigDecimal discount = percentageCoupon.calculateDiscount(orderAmount);

            // then - 10% of 30000 = 3000
            assertThat(discount).isEqualByComparingTo(BigDecimal.valueOf(3000));
        }

        @Test
        @DisplayName("should cap percentage discount at maximum")
        void capPercentageDiscountAtMaximum() {
            // given - max discount is 5000
            BigDecimal orderAmount = BigDecimal.valueOf(100000);

            // when - 10% of 100000 = 10000, but max is 5000
            BigDecimal discount = percentageCoupon.calculateDiscount(orderAmount);

            // then
            assertThat(discount).isEqualByComparingTo(BigDecimal.valueOf(5000));
        }

        @Test
        @DisplayName("should calculate fixed discount correctly")
        void calculateFixedDiscount() {
            // given
            BigDecimal orderAmount = BigDecimal.valueOf(30000);

            // when
            BigDecimal discount = fixedCoupon.calculateDiscount(orderAmount);

            // then
            assertThat(discount).isEqualByComparingTo(BigDecimal.valueOf(5000));
        }

        @Test
        @DisplayName("should not exceed order amount for fixed discount")
        void fixedDiscountNotExceedOrderAmount() {
            // given - order is 3000 but discount is 5000
            BigDecimal orderAmount = BigDecimal.valueOf(3000);

            // when - coupon requires minimum 20000, so should return 0
            BigDecimal discount = fixedCoupon.calculateDiscount(orderAmount);

            // then
            assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("should return zero if below minimum order")
        void returnZeroBelowMinimumOrder() {
            // given - minimum is 10000
            BigDecimal orderAmount = BigDecimal.valueOf(5000);

            // when
            BigDecimal discount = percentageCoupon.calculateDiscount(orderAmount);

            // then
            assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("should be valid within date range")
        void validWithinDateRange() {
            // then
            assertThat(percentageCoupon.isValid()).isTrue();
        }

        @Test
        @DisplayName("should be invalid before validFrom")
        void invalidBeforeValidFrom() {
            // given
            Coupon futureCoupon = Coupon.builder()
                    .code("future")
                    .name("Future")
                    .type(CouponType.FIXED_AMOUNT)
                    .discountValue(BigDecimal.valueOf(1000))
                    .validFrom(LocalDateTime.now().plusDays(1))
                    .validTo(LocalDateTime.now().plusDays(30))
                    .totalQuantity(10)
                    .build();

            // then
            assertThat(futureCoupon.isValid()).isFalse();
        }

        @Test
        @DisplayName("should be invalid after validTo")
        void invalidAfterValidTo() {
            // given
            Coupon expiredCoupon = Coupon.builder()
                    .code("expired")
                    .name("Expired")
                    .type(CouponType.FIXED_AMOUNT)
                    .discountValue(BigDecimal.valueOf(1000))
                    .validFrom(LocalDateTime.now().minusDays(30))
                    .validTo(LocalDateTime.now().minusDays(1))
                    .totalQuantity(10)
                    .build();

            // then
            assertThat(expiredCoupon.isValid()).isFalse();
        }

        @Test
        @DisplayName("should be invalid when quantity exhausted")
        void invalidWhenQuantityExhausted() {
            // given
            Coupon coupon = Coupon.builder()
                    .code("limited")
                    .name("Limited")
                    .type(CouponType.FIXED_AMOUNT)
                    .discountValue(BigDecimal.valueOf(1000))
                    .validFrom(LocalDateTime.now().minusDays(1))
                    .validTo(LocalDateTime.now().plusDays(1))
                    .totalQuantity(1)
                    .build();
            coupon.use();

            // then
            assertThat(coupon.isValid()).isFalse();
        }

        @Test
        @DisplayName("should be invalid when deactivated")
        void invalidWhenDeactivated() {
            // given
            percentageCoupon.deactivate();

            // then
            assertThat(percentageCoupon.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("Usage")
    class Usage {

        @Test
        @DisplayName("should increment used quantity")
        void incrementUsedQuantity() {
            // given
            int initialUsed = percentageCoupon.getUsedQuantity();

            // when
            percentageCoupon.use();

            // then
            assertThat(percentageCoupon.getUsedQuantity()).isEqualTo(initialUsed + 1);
        }

        @Test
        @DisplayName("should restore used quantity on cancel")
        void restoreUsedQuantity() {
            // given
            percentageCoupon.use();
            int usedAfterUse = percentageCoupon.getUsedQuantity();

            // when
            percentageCoupon.restoreQuantity();

            // then
            assertThat(percentageCoupon.getUsedQuantity()).isEqualTo(usedAfterUse - 1);
        }

        @Test
        @DisplayName("should throw exception when no quantity left")
        void throwOnNoQuantityLeft() {
            // given
            Coupon coupon = Coupon.builder()
                    .code("one")
                    .name("One Use")
                    .type(CouponType.FIXED_AMOUNT)
                    .discountValue(BigDecimal.valueOf(1000))
                    .validFrom(LocalDateTime.now().minusDays(1))
                    .validTo(LocalDateTime.now().plusDays(1))
                    .totalQuantity(1)
                    .build();
            coupon.use();

            // when & then
            assertThatThrownBy(coupon::use)
                    .isInstanceOf(InvalidStateException.class);
        }

        @Test
        @DisplayName("should calculate remaining quantity")
        void calculateRemainingQuantity() {
            // given
            percentageCoupon.use();
            percentageCoupon.use();

            // when
            int remaining = percentageCoupon.getRemainingQuantity();

            // then
            assertThat(remaining).isEqualTo(98); // 100 - 2
        }
    }

    @Nested
    @DisplayName("Activation")
    class Activation {

        @Test
        @DisplayName("should activate coupon")
        void activateCoupon() {
            // given
            percentageCoupon.deactivate();

            // when
            percentageCoupon.activate();

            // then
            assertThat(percentageCoupon.isActive()).isTrue();
        }

        @Test
        @DisplayName("should deactivate coupon")
        void deactivateCoupon() {
            // when
            percentageCoupon.deactivate();

            // then
            assertThat(percentageCoupon.isActive()).isFalse();
        }
    }
}

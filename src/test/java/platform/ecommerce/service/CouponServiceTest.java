package platform.ecommerce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import platform.ecommerce.domain.coupon.Coupon;
import platform.ecommerce.domain.coupon.CouponType;
import platform.ecommerce.domain.coupon.MemberCoupon;
import platform.ecommerce.dto.request.coupon.CouponCreateRequest;
import platform.ecommerce.dto.response.coupon.CouponApplyResponse;
import platform.ecommerce.dto.response.coupon.CouponResponse;
import platform.ecommerce.dto.response.coupon.MemberCouponResponse;
import platform.ecommerce.exception.EntityNotFoundException;
import platform.ecommerce.exception.InvalidStateException;
import platform.ecommerce.repository.coupon.CouponRepository;
import platform.ecommerce.repository.coupon.MemberCouponRepository;
import platform.ecommerce.service.coupon.CouponServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for CouponService.
 */
@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private MemberCouponRepository memberCouponRepository;

    @InjectMocks
    private CouponServiceImpl couponService;

    private Coupon testCoupon;
    private MemberCoupon testMemberCoupon;
    private static final Long COUPON_ID = 1L;
    private static final Long MEMBER_ID = 100L;
    private static final Long MEMBER_COUPON_ID = 50L;

    @BeforeEach
    void setUp() {
        testCoupon = Coupon.builder()
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
        ReflectionTestUtils.setField(testCoupon, "id", COUPON_ID);

        testMemberCoupon = MemberCoupon.builder()
                .memberId(MEMBER_ID)
                .coupon(testCoupon)
                .build();
        ReflectionTestUtils.setField(testMemberCoupon, "id", MEMBER_COUPON_ID);
    }

    @Nested
    @DisplayName("createCoupon")
    class CreateCoupon {

        @Test
        @DisplayName("should create coupon with all properties")
        void createCouponSuccessfully() {
            // given
            CouponCreateRequest request = CouponCreateRequest.builder()
                    .code("NEW20")
                    .name("20% Off")
                    .type(CouponType.PERCENTAGE)
                    .discountValue(BigDecimal.valueOf(20))
                    .minimumOrder(BigDecimal.valueOf(15000))
                    .maximumDiscount(BigDecimal.valueOf(10000))
                    .validFrom(LocalDateTime.now())
                    .validTo(LocalDateTime.now().plusDays(30))
                    .totalQuantity(50)
                    .build();

            given(couponRepository.existsByCode("NEW20")).willReturn(false);
            given(couponRepository.save(any(Coupon.class))).willAnswer(invocation -> {
                Coupon coupon = invocation.getArgument(0);
                ReflectionTestUtils.setField(coupon, "id", 2L);
                return coupon;
            });

            // when
            CouponResponse response = couponService.createCoupon(request);

            // then
            assertThat(response.code()).isEqualTo("NEW20");
            assertThat(response.type()).isEqualTo(CouponType.PERCENTAGE);
            assertThat(response.totalQuantity()).isEqualTo(50);
            verify(couponRepository).save(any(Coupon.class));
        }

        @Test
        @DisplayName("should throw exception for duplicate code")
        void throwOnDuplicateCode() {
            // given
            CouponCreateRequest request = CouponCreateRequest.builder()
                    .code("SAVE10")
                    .name("Duplicate")
                    .type(CouponType.FIXED_AMOUNT)
                    .discountValue(BigDecimal.valueOf(1000))
                    .validFrom(LocalDateTime.now())
                    .validTo(LocalDateTime.now().plusDays(1))
                    .totalQuantity(10)
                    .build();

            given(couponRepository.existsByCode("SAVE10")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> couponService.createCoupon(request))
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    @Nested
    @DisplayName("getCoupon")
    class GetCoupon {

        @Test
        @DisplayName("should return coupon by id")
        void returnCouponById() {
            // given
            given(couponRepository.findById(COUPON_ID)).willReturn(Optional.of(testCoupon));

            // when
            CouponResponse response = couponService.getCoupon(COUPON_ID);

            // then
            assertThat(response.id()).isEqualTo(COUPON_ID);
            assertThat(response.code()).isEqualTo("SAVE10");
        }

        @Test
        @DisplayName("should throw exception when not found")
        void throwOnNotFound() {
            // given
            given(couponRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> couponService.getCoupon(999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getCouponByCode")
    class GetCouponByCode {

        @Test
        @DisplayName("should return coupon by code")
        void returnCouponByCode() {
            // given
            given(couponRepository.findByCode("SAVE10")).willReturn(Optional.of(testCoupon));

            // when
            CouponResponse response = couponService.getCouponByCode("save10");

            // then
            assertThat(response.code()).isEqualTo("SAVE10");
        }
    }

    @Nested
    @DisplayName("issueCouponToMember")
    class IssueCouponToMember {

        @Test
        @DisplayName("should issue coupon to member")
        void issueCouponSuccessfully() {
            // given
            given(couponRepository.findById(COUPON_ID)).willReturn(Optional.of(testCoupon));
            given(memberCouponRepository.existsByMemberIdAndCouponId(MEMBER_ID, COUPON_ID)).willReturn(false);
            given(memberCouponRepository.save(any(MemberCoupon.class))).willAnswer(invocation -> {
                MemberCoupon mc = invocation.getArgument(0);
                ReflectionTestUtils.setField(mc, "id", MEMBER_COUPON_ID);
                return mc;
            });

            // when
            MemberCouponResponse response = couponService.issueCouponToMember(COUPON_ID, MEMBER_ID);

            // then
            assertThat(response.coupon().code()).isEqualTo("SAVE10");
            assertThat(response.used()).isFalse();
            verify(memberCouponRepository).save(any(MemberCoupon.class));
        }

        @Test
        @DisplayName("should throw exception when no quantity left")
        void throwOnNoQuantityLeft() {
            // given
            Coupon exhaustedCoupon = Coupon.builder()
                    .code("EXHAUSTED")
                    .name("Exhausted")
                    .type(CouponType.FIXED_AMOUNT)
                    .discountValue(BigDecimal.valueOf(1000))
                    .validFrom(LocalDateTime.now().minusDays(1))
                    .validTo(LocalDateTime.now().plusDays(1))
                    .totalQuantity(1)
                    .build();
            exhaustedCoupon.use();
            ReflectionTestUtils.setField(exhaustedCoupon, "id", 2L);

            given(couponRepository.findById(2L)).willReturn(Optional.of(exhaustedCoupon));

            // when & then
            assertThatThrownBy(() -> couponService.issueCouponToMember(2L, MEMBER_ID))
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    @Nested
    @DisplayName("getMemberCoupons")
    class GetMemberCoupons {

        @Test
        @DisplayName("should return member's coupons")
        void returnMemberCoupons() {
            // given
            given(memberCouponRepository.findAllByMemberId(MEMBER_ID))
                    .willReturn(List.of(testMemberCoupon));

            // when
            List<MemberCouponResponse> response = couponService.getMemberCoupons(MEMBER_ID);

            // then
            assertThat(response).hasSize(1);
            assertThat(response.get(0).coupon().code()).isEqualTo("SAVE10");
        }
    }

    @Nested
    @DisplayName("applyCoupon")
    class ApplyCoupon {

        @Test
        @DisplayName("should calculate discount for valid coupon")
        void calculateDiscountSuccessfully() {
            // given
            BigDecimal orderAmount = BigDecimal.valueOf(30000);
            given(couponRepository.findByCode("SAVE10")).willReturn(Optional.of(testCoupon));

            // when
            CouponApplyResponse response = couponService.applyCoupon("SAVE10", orderAmount);

            // then
            assertThat(response.originalAmount()).isEqualByComparingTo(BigDecimal.valueOf(30000));
            assertThat(response.discountAmount()).isEqualByComparingTo(BigDecimal.valueOf(3000)); // 10%
            assertThat(response.finalAmount()).isEqualByComparingTo(BigDecimal.valueOf(27000));
        }

        @Test
        @DisplayName("should throw exception for invalid coupon")
        void throwOnInvalidCoupon() {
            // given - below minimum order
            BigDecimal orderAmount = BigDecimal.valueOf(5000);
            given(couponRepository.findByCode("SAVE10")).willReturn(Optional.of(testCoupon));

            // when & then
            assertThatThrownBy(() -> couponService.applyCoupon("SAVE10", orderAmount))
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    @Nested
    @DisplayName("useCoupon")
    class UseCoupon {

        @Test
        @DisplayName("should mark member coupon as used")
        void useCouponSuccessfully() {
            // given
            given(memberCouponRepository.findByIdWithCoupon(MEMBER_COUPON_ID))
                    .willReturn(Optional.of(testMemberCoupon));

            // when
            couponService.useCoupon(MEMBER_COUPON_ID, 1000L);

            // then
            assertThat(testMemberCoupon.isUsed()).isTrue();
            assertThat(testMemberCoupon.getOrderId()).isEqualTo(1000L);
        }
    }

    @Nested
    @DisplayName("restoreCoupon")
    class RestoreCoupon {

        @Test
        @DisplayName("should restore used coupon")
        void restoreCouponSuccessfully() {
            // given
            testMemberCoupon.use(1000L);
            assertThat(testMemberCoupon.isUsed()).isTrue();

            given(memberCouponRepository.findByIdWithCoupon(MEMBER_COUPON_ID))
                    .willReturn(Optional.of(testMemberCoupon));

            // when
            couponService.restoreCoupon(MEMBER_COUPON_ID);

            // then
            assertThat(testMemberCoupon.isUsed()).isFalse();
        }
    }

    @Nested
    @DisplayName("validateCoupon")
    class ValidateCoupon {

        @Test
        @DisplayName("should return true for valid coupon")
        void returnTrueForValidCoupon() {
            // given
            given(couponRepository.findByCode("SAVE10")).willReturn(Optional.of(testCoupon));

            // when
            boolean valid = couponService.validateCoupon("SAVE10", BigDecimal.valueOf(20000));

            // then
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("should return false when below minimum order")
        void returnFalseWhenBelowMinimum() {
            // given
            given(couponRepository.findByCode("SAVE10")).willReturn(Optional.of(testCoupon));

            // when
            boolean valid = couponService.validateCoupon("SAVE10", BigDecimal.valueOf(5000));

            // then
            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("should return false when coupon not found")
        void returnFalseWhenNotFound() {
            // given
            given(couponRepository.findByCode("INVALID")).willReturn(Optional.empty());

            // when
            boolean valid = couponService.validateCoupon("INVALID", BigDecimal.valueOf(20000));

            // then
            assertThat(valid).isFalse();
        }
    }
}

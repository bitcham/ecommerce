package platform.ecommerce.service.coupon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import platform.ecommerce.domain.coupon.Coupon;
import platform.ecommerce.domain.coupon.MemberCoupon;
import platform.ecommerce.dto.request.coupon.CouponCreateRequest;
import platform.ecommerce.dto.request.coupon.CouponSearchCondition;
import platform.ecommerce.dto.request.coupon.CouponUpdateRequest;
import platform.ecommerce.dto.response.PageResponse;
import platform.ecommerce.dto.response.coupon.CouponApplyResponse;
import platform.ecommerce.dto.response.coupon.CouponCalculationResponse;
import platform.ecommerce.dto.response.coupon.CouponResponse;
import platform.ecommerce.dto.response.coupon.MemberCouponResponse;
import platform.ecommerce.exception.EntityNotFoundException;
import platform.ecommerce.exception.ErrorCode;
import platform.ecommerce.exception.InvalidStateException;
import platform.ecommerce.mapper.CouponMapper;
import platform.ecommerce.repository.coupon.CouponRepository;
import platform.ecommerce.repository.coupon.MemberCouponRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Coupon service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final CouponMapper couponMapper;

    @Override
    @Transactional
    public CouponResponse createCoupon(CouponCreateRequest request) {
        log.info("Creating coupon: {}", request.code());

        validateCodeUnique(request.code());

        Coupon coupon = Coupon.builder()
                .code(request.code())
                .name(request.name())
                .type(request.type())
                .discountValue(request.discountValue())
                .minimumOrder(request.minimumOrder())
                .maximumDiscount(request.maximumDiscount())
                .validFrom(request.validFrom())
                .validTo(request.validTo())
                .totalQuantity(request.totalQuantity())
                .build();

        Coupon savedCoupon = couponRepository.save(coupon);
        log.info("Coupon created: id={}", savedCoupon.getId());

        return couponMapper.toResponse(savedCoupon);
    }

    @Override
    public CouponResponse getCoupon(Long couponId) {
        Coupon coupon = findCouponById(couponId);
        return couponMapper.toResponse(coupon);
    }

    @Override
    public CouponResponse getCouponByCode(String code) {
        Coupon coupon = findCouponByCode(code);
        return couponMapper.toResponse(coupon);
    }

    @Override
    public PageResponse<CouponResponse> searchCoupons(CouponSearchCondition condition, Pageable pageable) {
        Page<Coupon> page = couponRepository.searchCoupons(condition, pageable);
        return PageResponse.of(page.map(couponMapper::toResponse));
    }

    @Override
    @Transactional
    public CouponResponse updateCoupon(Long couponId, CouponUpdateRequest request) {
        log.info("Updating coupon: {}", couponId);

        Coupon coupon = findCouponById(couponId);

        if (request.name() != null) {
            coupon.updateName(request.name());
        }
        if (request.minOrderAmount() != null) {
            coupon.updateMinimumOrder(request.minOrderAmount());
        }
        if (request.maxDiscountAmount() != null) {
            coupon.updateMaximumDiscount(request.maxDiscountAmount());
        }
        if (request.validFrom() != null || request.validUntil() != null) {
            coupon.updateValidPeriod(
                    request.validFrom() != null ? request.validFrom() : coupon.getValidFrom(),
                    request.validUntil() != null ? request.validUntil() : coupon.getValidTo()
            );
        }

        log.info("Coupon updated: id={}", couponId);
        return couponMapper.toResponse(coupon);
    }

    @Override
    @Transactional
    public void deactivateCoupon(Long couponId) {
        log.info("Deactivating coupon: {}", couponId);
        Coupon coupon = findCouponById(couponId);
        coupon.deactivate();
        log.info("Coupon deactivated: id={}", couponId);
    }

    @Override
    @Transactional
    public void deleteCoupon(Long couponId) {
        log.info("Deleting coupon: {}", couponId);
        Coupon coupon = findCouponById(couponId);
        coupon.delete();
        log.info("Coupon deleted: id={}", couponId);
    }

    @Override
    @Transactional
    public MemberCouponResponse issueCoupon(Long couponId, Long memberId) {
        return issueCouponToMember(couponId, memberId);
    }

    @Override
    @Transactional
    public MemberCouponResponse issueCouponByCode(String code, Long memberId) {
        Coupon coupon = findCouponByCode(code);
        return issueCouponToMember(coupon.getId(), memberId);
    }

    @Override
    @Transactional
    public MemberCouponResponse issueCouponToMember(Long couponId, Long memberId) {
        log.info("Issuing coupon to member: couponId={}, memberId={}", couponId, memberId);

        Coupon coupon = findCouponById(couponId);

        if (!coupon.hasQuantityAvailable()) {
            throw new InvalidStateException(ErrorCode.COUPON_LIMIT_EXCEEDED);
        }

        if (memberCouponRepository.existsByMemberIdAndCouponId(memberId, couponId)) {
            throw new InvalidStateException(ErrorCode.CONFLICT, "Coupon already issued to member");
        }

        MemberCoupon memberCoupon = MemberCoupon.builder()
                .memberId(memberId)
                .coupon(coupon)
                .build();

        MemberCoupon saved = memberCouponRepository.save(memberCoupon);
        log.info("Coupon issued: memberCouponId={}", saved.getId());

        return couponMapper.toMemberCouponResponse(saved);
    }

    @Override
    public List<MemberCouponResponse> getMemberCoupons(Long memberId) {
        return memberCouponRepository.findAllByMemberId(memberId).stream()
                .map(couponMapper::toMemberCouponResponse)
                .toList();
    }

    @Override
    public List<MemberCouponResponse> getAvailableMemberCoupons(Long memberId) {
        return memberCouponRepository.findAvailableByMemberId(memberId).stream()
                .filter(MemberCoupon::isAvailable)
                .map(couponMapper::toMemberCouponResponse)
                .toList();
    }

    @Override
    public PageResponse<MemberCouponResponse> getMemberCoupons(Long memberId, boolean availableOnly, Pageable pageable) {
        Page<MemberCoupon> page;
        if (availableOnly) {
            page = memberCouponRepository.findAvailableByMemberId(memberId, pageable);
        } else {
            page = memberCouponRepository.findAllByMemberId(memberId, pageable);
        }
        return PageResponse.of(page.map(couponMapper::toMemberCouponResponse));
    }

    @Override
    public CouponCalculationResponse calculateDiscount(Long couponId, BigDecimal orderAmount) {
        Coupon coupon = findCouponById(couponId);

        if (!coupon.isApplicable(orderAmount)) {
            return CouponCalculationResponse.notApplicable(
                    couponId, coupon.getCode(), coupon.getName(), orderAmount,
                    "Order amount does not meet minimum requirement"
            );
        }

        BigDecimal discountAmount = coupon.calculateDiscount(orderAmount);
        return CouponCalculationResponse.applicable(
                couponId, coupon.getCode(), coupon.getName(), orderAmount, discountAmount
        );
    }

    @Override
    public PageResponse<MemberCouponResponse> getAvailableCouponsForOrder(Long memberId, BigDecimal orderAmount, Pageable pageable) {
        Page<MemberCoupon> page = memberCouponRepository.findAvailableByMemberId(memberId, pageable);

        // Filter only applicable coupons
        List<MemberCouponResponse> applicable = page.getContent().stream()
                .filter(mc -> mc.getCoupon().isApplicable(orderAmount))
                .map(couponMapper::toMemberCouponResponse)
                .toList();

        return PageResponse.of(applicable, (int) page.getTotalElements(), pageable.getPageNumber(), pageable.getPageSize());
    }

    @Override
    public CouponApplyResponse applyCoupon(String code, BigDecimal orderAmount) {
        log.info("Applying coupon: code={}, orderAmount={}", code, orderAmount);

        Coupon coupon = findCouponByCode(code);

        if (!coupon.isApplicable(orderAmount)) {
            throw new InvalidStateException(ErrorCode.COUPON_NOT_APPLICABLE);
        }

        BigDecimal discountAmount = coupon.calculateDiscount(orderAmount);
        BigDecimal finalAmount = orderAmount.subtract(discountAmount);

        return CouponApplyResponse.builder()
                .code(code)
                .originalAmount(orderAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .build();
    }

    @Override
    @Transactional
    public void useCoupon(Long memberCouponId, Long orderId) {
        log.info("Using coupon: memberCouponId={}, orderId={}", memberCouponId, orderId);

        MemberCoupon memberCoupon = memberCouponRepository.findByIdWithCoupon(memberCouponId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.COUPON_NOT_FOUND));

        memberCoupon.use(orderId);

        log.info("Coupon used: memberCouponId={}", memberCouponId);
    }

    @Override
    @Transactional
    public void restoreCoupon(Long memberCouponId) {
        log.info("Restoring coupon: memberCouponId={}", memberCouponId);

        MemberCoupon memberCoupon = memberCouponRepository.findByIdWithCoupon(memberCouponId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.COUPON_NOT_FOUND));

        memberCoupon.restore();

        log.info("Coupon restored: memberCouponId={}", memberCouponId);
    }

    @Override
    public boolean validateCoupon(String code, BigDecimal orderAmount) {
        try {
            Coupon coupon = findCouponByCode(code);
            return coupon.isApplicable(orderAmount);
        } catch (EntityNotFoundException e) {
            return false;
        }
    }

    // ========== Private Helper Methods ==========

    private Coupon findCouponById(Long couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.COUPON_NOT_FOUND));
    }

    private Coupon findCouponByCode(String code) {
        return couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.COUPON_NOT_FOUND));
    }

    private void validateCodeUnique(String code) {
        if (couponRepository.existsByCode(code.toUpperCase())) {
            throw new InvalidStateException(ErrorCode.CONFLICT, "Coupon code already exists");
        }
    }
}

package platform.ecommerce.domain.auth;

import jakarta.persistence.*;
import lombok.*;
import platform.ecommerce.domain.common.BaseEntity;
import platform.ecommerce.domain.member.Member;
import platform.ecommerce.exception.ErrorCode;
import platform.ecommerce.exception.InvalidStateException;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Email verification token entity.
 * Used for verifying user email during registration.
 */
@Entity
@Table(name = "email_verification_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerificationToken extends BaseEntity {

    private static final int EXPIRATION_HOURS = 24;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "is_used", nullable = false)
    private boolean used;

    @Builder
    public EmailVerificationToken(Member member) {
        this.token = UUID.randomUUID().toString();
        this.member = member;
        this.expiresAt = LocalDateTime.now().plusHours(EXPIRATION_HOURS);
        this.used = false;
    }

    /**
     * Verify the token and mark as used.
     */
    public void verify() {
        validateNotExpired();
        validateNotUsed();

        this.used = true;
        this.verifiedAt = LocalDateTime.now();
        this.member.verifyEmail();
    }

    /**
     * Check if token is expired.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if token is valid (not expired and not used).
     */
    public boolean isValid() {
        return !isExpired() && !used;
    }

    private void validateNotExpired() {
        if (isExpired()) {
            throw new InvalidStateException(ErrorCode.TOKEN_EXPIRED);
        }
    }

    private void validateNotUsed() {
        if (used) {
            throw new InvalidStateException(ErrorCode.TOKEN_INVALID, "Token already used");
        }
    }
}

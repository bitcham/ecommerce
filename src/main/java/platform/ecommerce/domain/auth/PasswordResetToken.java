package platform.ecommerce.domain.auth;

import jakarta.persistence.*;
import lombok.*;
import platform.ecommerce.domain.common.BaseTimeEntity;
import platform.ecommerce.domain.member.Member;
import platform.ecommerce.exception.ErrorCode;
import platform.ecommerce.exception.InvalidStateException;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Password reset token entity.
 * Used for resetting user password via email link.
 */
@Entity
@Table(name = "password_reset_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetToken extends BaseTimeEntity {

    private static final int EXPIRATION_HOURS = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "is_used", nullable = false)
    private boolean used;

    @Builder
    public PasswordResetToken(Member member) {
        this.token = UUID.randomUUID().toString();
        this.member = member;
        this.expiresAt = LocalDateTime.now().plusHours(EXPIRATION_HOURS);
        this.used = false;
    }

    /**
     * Use the token and mark as used.
     */
    public void use() {
        validateNotExpired();
        validateNotUsed();

        this.used = true;
        this.usedAt = LocalDateTime.now();
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

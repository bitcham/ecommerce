package platform.ecommerce.domain.auth;

import jakarta.persistence.*;
import lombok.*;
import platform.ecommerce.domain.common.BaseTimeEntity;
import platform.ecommerce.domain.member.Member;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Refresh token entity.
 * Used for issuing new access tokens without re-authentication.
 */
@Entity
@Table(name = "refresh_token", indexes = {
        @Index(name = "idx_refresh_token_token", columnList = "token"),
        @Index(name = "idx_refresh_token_member", columnList = "member_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseTimeEntity {

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

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(nullable = false)
    private boolean revoked;

    @Builder
    public RefreshToken(Member member, long expirationDays, String deviceInfo, String ipAddress) {
        this.token = UUID.randomUUID().toString();
        this.member = member;
        this.expiresAt = LocalDateTime.now().plusDays(expirationDays);
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
        this.revoked = false;
    }

    /**
     * Check if token is expired.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if token is valid (not expired and not revoked).
     */
    public boolean isValid() {
        return !isExpired() && !revoked;
    }

    /**
     * Revoke the token.
     */
    public void revoke() {
        this.revoked = true;
    }

    /**
     * Rotate token - generates new token value and extends expiration.
     */
    public String rotate(long expirationDays) {
        this.token = UUID.randomUUID().toString();
        this.expiresAt = LocalDateTime.now().plusDays(expirationDays);
        return this.token;
    }
}

package platform.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import platform.ecommerce.domain.auth.RefreshToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Refresh token repository.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find token by token string.
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find valid token by token string.
     */
    @Query("SELECT t FROM RefreshToken t WHERE t.token = :token " +
           "AND t.revoked = false AND t.expiresAt > :now")
    Optional<RefreshToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    /**
     * Find all tokens for a member.
     */
    List<RefreshToken> findByMemberId(Long memberId);

    /**
     * Revoke all tokens for a member.
     */
    @Modifying
    @Query("UPDATE RefreshToken t SET t.revoked = true WHERE t.member.id = :memberId")
    int revokeAllByMemberId(@Param("memberId") Long memberId);

    /**
     * Delete expired and revoked tokens.
     */
    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.expiresAt < :now OR t.revoked = true")
    int deleteExpiredAndRevokedTokens(@Param("now") LocalDateTime now);

    /**
     * Count active tokens for a member.
     */
    @Query("SELECT COUNT(t) FROM RefreshToken t WHERE t.member.id = :memberId " +
           "AND t.revoked = false AND t.expiresAt > :now")
    long countActiveTokensByMemberId(@Param("memberId") Long memberId, @Param("now") LocalDateTime now);
}

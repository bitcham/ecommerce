package platform.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import platform.ecommerce.domain.auth.EmailVerificationToken;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Email verification token repository.
 */
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    /**
     * Find token by token string.
     */
    Optional<EmailVerificationToken> findByToken(String token);

    /**
     * Find latest unused token by member ID.
     */
    @Query("SELECT t FROM EmailVerificationToken t WHERE t.member.id = :memberId " +
           "AND t.used = false AND t.expiresAt > :now ORDER BY t.createdAt DESC")
    Optional<EmailVerificationToken> findLatestValidTokenByMemberId(
            @Param("memberId") Long memberId,
            @Param("now") LocalDateTime now);

    /**
     * Delete expired tokens.
     */
    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Delete all tokens for a member.
     */
    @Modifying
    void deleteByMemberId(Long memberId);
}

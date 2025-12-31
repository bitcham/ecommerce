package platform.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import platform.ecommerce.domain.auth.PasswordResetToken;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for password reset tokens.
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    @Query("SELECT t FROM PasswordResetToken t WHERE t.token = :token AND t.used = false AND t.expiresAt > :now")
    Optional<PasswordResetToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    @Query("SELECT t FROM PasswordResetToken t WHERE t.member.id = :memberId AND t.used = false AND t.expiresAt > :now ORDER BY t.createdAt DESC")
    Optional<PasswordResetToken> findLatestValidTokenByMemberId(@Param("memberId") Long memberId, @Param("now") LocalDateTime now);
}

package platform.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import platform.ecommerce.domain.member.Member;
import platform.ecommerce.domain.member.MemberStatus;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Member JPA repository.
 */
public interface MemberRepository extends JpaRepository<Member, Long>, MemberQueryRepository {

    /**
     * Find member by email.
     */
    Optional<Member> findByEmail(String email);

    /**
     * Find active member by email (excluding withdrawn).
     */
    @Query("SELECT m FROM Member m WHERE m.email = :email AND m.status != :excludedStatus")
    Optional<Member> findByEmailAndStatusNot(@Param("email") String email,
                                              @Param("excludedStatus") MemberStatus excludedStatus);

    /**
     * Check if email exists.
     */
    boolean existsByEmail(String email);

    /**
     * Check if email exists excluding specific member.
     */
    @Query("SELECT COUNT(m) > 0 FROM Member m WHERE m.email = :email AND m.id != :memberId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("memberId") Long memberId);

    /**
     * Count members created between dates.
     */
    @Query("SELECT COUNT(m) FROM Member m WHERE m.createdAt BETWEEN :from AND :to")
    long countByCreatedAtBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}

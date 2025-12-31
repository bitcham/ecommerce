package platform.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import platform.ecommerce.domain.member.MemberAddress;

import java.util.List;

/**
 * Member address JPA repository.
 */
public interface MemberAddressRepository extends JpaRepository<MemberAddress, Long> {

    /**
     * Find all addresses by member ID.
     */
    List<MemberAddress> findByMemberId(Long memberId);

    /**
     * Count addresses by member ID.
     */
    int countByMemberId(Long memberId);
}

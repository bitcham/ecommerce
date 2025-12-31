package platform.ecommerce.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import platform.ecommerce.domain.member.Member;
import platform.ecommerce.dto.request.MemberSearchCondition;

/**
 * Member custom query repository interface.
 */
public interface MemberQueryRepository {

    /**
     * Search members with dynamic conditions.
     */
    Page<Member> searchMembers(MemberSearchCondition condition, Pageable pageable);
}

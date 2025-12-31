package platform.ecommerce.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import platform.ecommerce.domain.member.Member;
import platform.ecommerce.domain.member.MemberRole;
import platform.ecommerce.domain.member.MemberStatus;
import platform.ecommerce.dto.request.MemberSearchCondition;

import java.util.List;

import static platform.ecommerce.domain.member.QMember.member;

/**
 * Member QueryDSL repository implementation.
 */
@Repository
@RequiredArgsConstructor
public class MemberQueryRepositoryImpl implements MemberQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Member> searchMembers(MemberSearchCondition condition, Pageable pageable) {
        List<Member> content = queryFactory
                .selectFrom(member)
                .where(
                        emailContains(condition.email()),
                        nameContains(condition.name()),
                        statusEquals(condition.status()),
                        roleEquals(condition.role()),
                        excludeWithdrawn(condition.excludeWithdrawn())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(member.createdAt.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(member.count())
                .from(member)
                .where(
                        emailContains(condition.email()),
                        nameContains(condition.name()),
                        statusEquals(condition.status()),
                        roleEquals(condition.role()),
                        excludeWithdrawn(condition.excludeWithdrawn())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression emailContains(String email) {
        return email != null && !email.isBlank() ? member.email.containsIgnoreCase(email) : null;
    }

    private BooleanExpression nameContains(String name) {
        return name != null && !name.isBlank() ? member.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression statusEquals(MemberStatus status) {
        return status != null ? member.status.eq(status) : null;
    }

    private BooleanExpression roleEquals(MemberRole role) {
        return role != null ? member.role.eq(role) : null;
    }

    private BooleanExpression excludeWithdrawn(boolean exclude) {
        return exclude ? member.status.ne(MemberStatus.WITHDRAWN) : null;
    }
}

package platform.ecommerce.dto.response;

import lombok.Builder;
import platform.ecommerce.domain.member.MemberRole;
import platform.ecommerce.domain.member.MemberStatus;

import java.time.LocalDateTime;

/**
 * Member response DTO.
 */
@Builder
public record MemberResponse(
        Long id,
        String email,
        String name,
        String phone,
        String profileImage,
        MemberRole role,
        MemberStatus status,
        boolean emailVerified,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
}

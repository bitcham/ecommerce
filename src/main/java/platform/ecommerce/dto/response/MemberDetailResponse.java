package platform.ecommerce.dto.response;

import lombok.Builder;
import platform.ecommerce.domain.member.MemberRole;
import platform.ecommerce.domain.member.MemberStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Member detail response DTO with addresses.
 */
@Builder
public record MemberDetailResponse(
        Long id,
        String email,
        String name,
        String phone,
        String profileImage,
        MemberRole role,
        MemberStatus status,
        boolean emailVerified,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        List<AddressResponse> addresses,
        AddressResponse defaultAddress
) {
}

package platform.ecommerce.mapper

import org.mapstruct.Mapper
import platform.ecommerce.domain.Member
import platform.ecommerce.dto.response.MemberResponse

@Mapper
interface MemberMapper {
    fun toResponse(member: Member): MemberResponse
}

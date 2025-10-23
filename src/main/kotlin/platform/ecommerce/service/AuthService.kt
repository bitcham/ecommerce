package platform.ecommerce.service

import org.springframework.stereotype.Service
import platform.ecommerce.dto.request.MemberRegister
import platform.ecommerce.dto.response.MemberResponse
import platform.ecommerce.mapper.MemberMapper

@Service
class AuthService(
    private val memberService: MemberService,
    private val memberMapper: MemberMapper
) {
    fun register(request: MemberRegister): MemberResponse {
        val member = memberService.register(request)
        return memberMapper.toResponse(member)
    }
}
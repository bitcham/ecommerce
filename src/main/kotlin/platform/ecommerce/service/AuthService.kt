package platform.ecommerce.service

import platform.ecommerce.domain.Member
import platform.ecommerce.dto.request.LoginRequest
import platform.ecommerce.dto.request.MemberRegister
import platform.ecommerce.dto.response.LoginResponse

interface AuthService {
    fun register(request: MemberRegister): Member
    fun login(request: LoginRequest): LoginResponse
}
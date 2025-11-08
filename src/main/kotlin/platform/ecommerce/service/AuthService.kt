package platform.ecommerce.service

import platform.ecommerce.dto.request.LoginRequest
import platform.ecommerce.dto.request.MemberRegister
import platform.ecommerce.dto.response.LoginResponse
import platform.ecommerce.dto.response.MemberResponse
import java.util.UUID

interface AuthService {
    fun register(request: MemberRegister): MemberResponse
    fun login(request: LoginRequest): LoginResponse
}
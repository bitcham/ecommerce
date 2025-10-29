package platform.ecommerce.service.impl

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import platform.ecommerce.domain.Member
import platform.ecommerce.dto.request.LoginRequest
import platform.ecommerce.dto.request.MemberRegister
import platform.ecommerce.dto.response.LoginResponse
import platform.ecommerce.dto.response.MemberResponse
import platform.ecommerce.exception.InvalidCredentialsException
import platform.ecommerce.mapper.MemberMapper
import platform.ecommerce.security.JwtUtil
import platform.ecommerce.service.AuthService
import platform.ecommerce.service.MemberService
import platform.ecommerce.utils.Logger.Companion.logger

@Service
class AuthServiceImpl(
    private val memberService: MemberService,
    private val memberMapper: MemberMapper,
    private val authenticationManager: AuthenticationManager,
    private val jwtUtil: JwtUtil
): AuthService {
    override fun register(request: MemberRegister): MemberResponse {
        val member = memberService.register(request)
        return memberMapper.toResponse(member)
    }


    override fun login(request: LoginRequest): LoginResponse {
        try{
            val memberDetails = authenticationUser(request)
            val member = memberService.findByEmail(memberDetails.username)
            return buildLoginResponse(member, memberDetails)
        } catch(e: BadCredentialsException) {
            logger.warn{"Authentication failed for username: ${request.email}"}
            throw InvalidCredentialsException("Invalid username or password");
        }
    }

    private fun authenticationUser(request: LoginRequest): UserDetails {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                request.email,
                request.password
            )
        )
        return authentication.principal as UserDetails
    }

    private fun buildLoginResponse(member: Member, memberDetails: UserDetails): LoginResponse {
        val accessToken = jwtUtil.generateAccessToken(memberDetails)
        val refreshToken = jwtUtil.generateRefreshToken(memberDetails)
        val expiresIn = jwtUtil.getAccessTokenExpiration()

        return LoginResponse(
            id = member.id!!,
            email = member.email,
            role = member.role.name,
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = "Bearer",
            expiresIn = expiresIn
        )
    }
}

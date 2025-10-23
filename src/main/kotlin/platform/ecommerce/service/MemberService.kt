package platform.ecommerce.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import platform.ecommerce.domain.Member
import platform.ecommerce.dto.request.MemberRegister
import platform.ecommerce.exception.DuplicateEmailException
import platform.ecommerce.repository.MemberRepository

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun register(request: MemberRegister): Member {
        isDuplicatedEmail(request)

        val member = Member.register(
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            firstName = request.firstName,
            lastName = request.lastName,
            phone = request.phone
        )

        return memberRepository.save(member)
    }

    private fun isDuplicatedEmail(request: MemberRegister) {
        if (memberRepository.existsByEmail(request.email)) {
            throw DuplicateEmailException(request.email)
        }
    }
}
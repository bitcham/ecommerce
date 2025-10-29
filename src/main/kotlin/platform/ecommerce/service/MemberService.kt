package platform.ecommerce.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import platform.ecommerce.domain.Member
import platform.ecommerce.dto.request.MemberRegister
import platform.ecommerce.exception.DuplicateEmailException
import platform.ecommerce.exception.MemberNotFoundException
import platform.ecommerce.repository.MemberRepository
import platform.ecommerce.utils.Logger
import platform.ecommerce.utils.Logger.Companion.logger

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

        logger.debug{ "member created: memberId = ${member.id}" }

        return memberRepository.save(member)
    }

    fun findByEmail(email: String): Member {
        logger.debug { "Finding member by email: $email" }

        return memberRepository.findByEmail(email)
            ?: throw MemberNotFoundException("Member not found with email: $email")
    }

    private fun isDuplicatedEmail(request: MemberRegister) {
        if (memberRepository.existsByEmail(request.email)) {
            throw DuplicateEmailException("Email already exists: ${request.email}")
        }
    }
}
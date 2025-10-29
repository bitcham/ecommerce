package platform.ecommerce.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import platform.ecommerce.enums.MemberStatus
import platform.ecommerce.repository.MemberRepository

@Service
class MemberDetailsService(private val memberRepository: MemberRepository): UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val member = memberRepository.findByEmail(username)
            ?: throw UsernameNotFoundException("User not found with username: $username")

        if (member.status != MemberStatus.ACTIVE) {
            throw UsernameNotFoundException("User account is not active")
        }

        return User.builder()
            .username(member.email)
            .password(member.passwordHash)
            .authorities(setOf(SimpleGrantedAuthority("ROLE_${member.role.name}")))
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(false)
            .build()
    }
}
package platform.ecommerce.repository

import org.springframework.data.jpa.repository.JpaRepository
import platform.ecommerce.domain.Member
import java.util.Optional
import java.util.UUID

interface MemberRepository: JpaRepository<Member, UUID> {
    fun existsByEmail(email: String): Boolean

    fun findByEmail(email: String): Member?

}
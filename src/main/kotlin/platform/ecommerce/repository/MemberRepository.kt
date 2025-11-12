package platform.ecommerce.repository


import org.springframework.data.repository.Repository
import platform.ecommerce.domain.Member
import java.util.*

interface MemberRepository: Repository<Member, UUID> {
    fun existsByEmail(email: String): Boolean

    fun findByEmail(email: String): Member?

    fun save(member: Member): Member

}
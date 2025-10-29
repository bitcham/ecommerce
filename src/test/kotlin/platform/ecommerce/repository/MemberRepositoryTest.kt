package platform.ecommerce.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import platform.ecommerce.domain.Member
import platform.ecommerce.domain.vo.Address
import platform.ecommerce.enums.MemberRole
import platform.ecommerce.enums.MemberStatus

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Test
    fun `Should return true when email exists`() {
        // Given
        val member = Member.register(
            email = "existing@example.com",
            passwordHash = "hashedPassword",
            firstName = "John",
            lastName = "Doe"
        )
        entityManager.persist(member)
        entityManager.flush()

        // When
        val exists = memberRepository.existsByEmail("existing@example.com")

        // Then
        assertThat(exists).isTrue()
    }

    @Test
    fun `Should return false when email does not exist`() {
        // When
        val exists = memberRepository.existsByEmail("nonexistent@example.com")

        // Then
        assertThat(exists).isFalse()
    }

    @Test
    fun `Should be case-sensitive`() {
        // Given
        val member = Member.register(
            email = "test@example.com",
            passwordHash = "hashedPassword",
            firstName = "John",
            lastName = "Doe"
        )
        entityManager.persist(member)
        entityManager.flush()

        // When
        val existsUpperCase = memberRepository.existsByEmail("TEST@EXAMPLE.COM")

        // Then
        assertThat(existsUpperCase).isFalse()
    }

    @Test
    fun `Should save member with all required fields`() {
        // Given
        val member = Member.register(
            email = "test@example.com",
            passwordHash = "hashedPassword123",
            firstName = "John",
            lastName = "Doe",
            phone = "1234567890"
        )

        // When
        val saved = memberRepository.save(member)
        entityManager.flush()
        val found = entityManager.find(Member::class.java, saved.id)

        // Then
        assertThat(found).isNotNull
        assertThat(found.email).isEqualTo("test@example.com")
        assertThat(found.passwordHash).isEqualTo("hashedPassword123")
        assertThat(found.firstName).isEqualTo("John")
        assertThat(found.lastName).isEqualTo("Doe")
        assertThat(found.phone).isEqualTo("1234567890")
    }

    @Test
    fun `Should save member with default role as CUSTOMER`() {
        // Given
        val member = Member.register(
            email = "test@example.com",
            passwordHash = "hashedPassword",
            firstName = "John",
            lastName = "Doe"
        )

        // When
        val saved = memberRepository.save(member)
        entityManager.flush()

        // Then
        assertThat(saved.role).isEqualTo(MemberRole.CUSTOMER)
    }

    @Test
    fun `Should save member with default status as PENDING`() {
        // Given
        val member = Member.register(
            email = "test@example.com",
            passwordHash = "hashedPassword",
            firstName = "John",
            lastName = "Doe"
        )

        // When
        val saved = memberRepository.save(member)
        entityManager.flush()

        // Then
        assertThat(saved.status).isEqualTo(MemberStatus.PENDING)
    }

    @Test
    fun `Should generate UUID for id`() {
        // Given
        val member = Member.register(
            email = "test@example.com",
            passwordHash = "hashedPassword",
            firstName = "John",
            lastName = "Doe"
        )

        // When
        val saved = memberRepository.save(member)
        entityManager.flush()

        // Then
        assertThat(saved.id).isNotNull()
    }

    @Test
    fun `Should save member without optional phone`() {
        // Given
        val member = Member.register(
            email = "test@example.com",
            passwordHash = "hashedPassword",
            firstName = "John",
            lastName = "Doe",
            phone = null
        )

        // When
        val saved = memberRepository.save(member)
        entityManager.flush()
        val found = entityManager.find(Member::class.java, saved.id)

        // Then
        assertThat(found.phone).isNull()
    }

    @Test
    fun `Should set createdAt and updatedAt timestamps`() {
        // Given
        val member = Member.register(
            email = "test@example.com",
            passwordHash = "hashedPassword",
            firstName = "John",
            lastName = "Doe"
        )

        // When
        val saved = memberRepository.save(member)
        entityManager.flush()

        // Then
        assertThat(saved.createdAt).isNotNull()
        assertThat(saved.updatedAt).isNotNull()
    }

    @Test
    fun `Should save member with billing address`() {
        // Given
        val billingAddress = Address(
            streetAddress = "123 Main St",
            city = "New York",
            postalCode = "10001"
        )
        val member = Member.register(
            email = "test@example.com",
            passwordHash = "hashedPassword",
            firstName = "John",
            lastName = "Doe"
        )
        member.billingAddress = billingAddress

        // When
        val saved = memberRepository.save(member)
        entityManager.flush()
        entityManager.clear()
        val found = memberRepository.findById(saved.id!!).get()

        // Then
        assertThat(found.billingAddress).isNotNull
        assertThat(found.billingAddress?.streetAddress).isEqualTo("123 Main St")
        assertThat(found.billingAddress?.city).isEqualTo("New York")
        assertThat(found.billingAddress?.postalCode).isEqualTo("10001")
    }

    @Test
    fun `Should save member with delivery address`() {
        // Given
        val deliveryAddress = Address(
            streetAddress = "456 Oak Ave",
            city = "Los Angeles",
            postalCode = "90001"
        )
        val member = Member.register(
            email = "test@example.com",
            passwordHash = "hashedPassword",
            firstName = "John",
            lastName = "Doe"
        )
        member.deliveryAddress = deliveryAddress

        // When
        val saved = memberRepository.save(member)
        entityManager.flush()
        entityManager.clear()
        val found = memberRepository.findById(saved.id!!).get()

        // Then
        assertThat(found.deliveryAddress).isNotNull
        assertThat(found.deliveryAddress?.streetAddress).isEqualTo("456 Oak Ave")
        assertThat(found.deliveryAddress?.city).isEqualTo("Los Angeles")
        assertThat(found.deliveryAddress?.postalCode).isEqualTo("90001")
    }

    @Test
    fun `Should save member with both billing and delivery addresses`() {
        // Given
        val billingAddress = Address(
            streetAddress = "123 Main St",
            city = "New York",
            postalCode = "10001"
        )
        val deliveryAddress = Address(
            streetAddress = "456 Oak Ave",
            city = "Los Angeles",
            postalCode = "90001"
        )
        val member = Member.register(
            email = "test@example.com",
            passwordHash = "hashedPassword",
            firstName = "John",
            lastName = "Doe"
        )
        member.billingAddress = billingAddress
        member.deliveryAddress = deliveryAddress

        // When
        val saved = memberRepository.save(member)
        entityManager.flush()
        entityManager.clear()
        val found = memberRepository.findById(saved.id!!).get()

        // Then
        assertThat(found.billingAddress).isNotNull
        assertThat(found.deliveryAddress).isNotNull
        assertThat(found.billingAddress?.city).isEqualTo("New York")
        assertThat(found.deliveryAddress?.city).isEqualTo("Los Angeles")
    }

    @Test
    fun `Should find member by id`() {
        // Given
        val member = Member.register(
            email = "test@example.com",
            passwordHash = "hashedPassword",
            firstName = "John",
            lastName = "Doe"
        )
        val saved = entityManager.persist(member)
        entityManager.flush()
        entityManager.clear()

        // When
        val found = memberRepository.findById(saved.id!!)

        // Then
        assertThat(found).isPresent
        assertThat(found.get().email).isEqualTo("test@example.com")
    }

    @Test
    fun `Should return empty when member not found`() {
        // When
        val found = memberRepository.findById(java.util.UUID.randomUUID())

        // Then
        assertThat(found).isEmpty
    }

    @Test
    fun `Should update member fields`() {
        // Given
        val member = Member.register(
            email = "test@example.com",
            passwordHash = "oldPassword",
            firstName = "John",
            lastName = "Doe"
        )
        val saved = memberRepository.save(member)
        entityManager.flush()
        entityManager.clear()

        // When
        val found = memberRepository.findById(saved.id!!).get()
        found.firstName = "Jane"
        found.passwordHash = "newPassword"
        memberRepository.save(found)
        entityManager.flush()
        entityManager.clear()

        // Then
        val updated = memberRepository.findById(saved.id!!).get()
        assertThat(updated.firstName).isEqualTo("Jane")
        assertThat(updated.passwordHash).isEqualTo("newPassword")
    }

    @Test
    fun `Should update member status`() {
        // Given
        val member = Member.register(
            email = "test@example.com",
            passwordHash = "hashedPassword",
            firstName = "John",
            lastName = "Doe"
        )
        val saved = memberRepository.save(member)
        entityManager.flush()
        entityManager.clear()

        // When
        val found = memberRepository.findById(saved.id!!).get()
        found.status = MemberStatus.ACTIVE
        memberRepository.save(found)
        entityManager.flush()
        entityManager.clear()

        // Then
        val updated = memberRepository.findById(saved.id!!).get()
        assertThat(updated.status).isEqualTo(MemberStatus.ACTIVE)
    }

    @Test
    fun `Should update updatedAt timestamp on modification`() {
        // Given
        val member = Member.register(
            email = "test@example.com",
            passwordHash = "hashedPassword",
            firstName = "John",
            lastName = "Doe"
        )
        val saved = memberRepository.save(member)
        entityManager.flush()
        val originalUpdatedAt = saved.updatedAt

        // When
        Thread.sleep(100) // Ensure time difference
        val found = memberRepository.findById(saved.id!!).get()
        found.firstName = "Jane"
        memberRepository.save(found)
        entityManager.flush()

        // Then
        val updated = memberRepository.findById(saved.id!!).get()
        assertThat(updated.updatedAt).isAfter(originalUpdatedAt)
    }
}

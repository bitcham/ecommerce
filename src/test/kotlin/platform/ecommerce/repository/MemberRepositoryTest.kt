package platform.ecommerce.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import platform.ecommerce.domain.Member
import platform.ecommerce.domain.vo.Address
import platform.ecommerce.enums.MemberRole
import platform.ecommerce.enums.MemberStatus

@DataJpaTest
@DisplayName("MemberRepository")
class MemberRepositoryTest {

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Nested
    @DisplayName("existsByEmail")
    inner class ExistsByEmail {

        @Test
        @DisplayName("Should return true when email exists")
        fun shouldReturnTrueWhenEmailExists() {
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
        @DisplayName("Should return false when email does not exist")
        fun shouldReturnFalseWhenEmailDoesNotExist() {
            // When
            val exists = memberRepository.existsByEmail("nonexistent@example.com")

            // Then
            assertThat(exists).isFalse()
        }

        @Test
        @DisplayName("Should be case-sensitive")
        fun shouldBeCaseSensitive() {
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
    }

    @Nested
    @DisplayName("save")
    inner class Save {

        @Test
        @DisplayName("Should save member with all required fields")
        fun shouldSaveMemberWithRequiredFields() {
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
        @DisplayName("Should save member with default role as CUSTOMER")
        fun shouldSaveMemberWithDefaultRole() {
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
        @DisplayName("Should save member with default status as PENDING")
        fun shouldSaveMemberWithDefaultStatus() {
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
        @DisplayName("Should generate UUID for id")
        fun shouldGenerateUuidForId() {
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
        @DisplayName("Should save member without optional phone")
        fun shouldSaveMemberWithoutPhone() {
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
        @DisplayName("Should set createdAt and updatedAt timestamps")
        fun shouldSetTimestamps() {
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
    }

    @Nested
    @DisplayName("Embedded Address")
    inner class EmbeddedAddress {

        @Test
        @DisplayName("Should save member with billing address")
        fun shouldSaveMemberWithBillingAddress() {
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
        @DisplayName("Should save member with delivery address")
        fun shouldSaveMemberWithDeliveryAddress() {
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
        @DisplayName("Should save member with both billing and delivery addresses")
        fun shouldSaveMemberWithBothAddresses() {
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
    }

    @Nested
    @DisplayName("findById")
    inner class FindById {

        @Test
        @DisplayName("Should find member by id")
        fun shouldFindMemberById() {
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
        @DisplayName("Should return empty when member not found")
        fun shouldReturnEmptyWhenNotFound() {
            // When
            val found = memberRepository.findById(java.util.UUID.randomUUID())

            // Then
            assertThat(found).isEmpty
        }
    }

    @Nested
    @DisplayName("Update Operations")
    inner class UpdateOperations {

        @Test
        @DisplayName("Should update member fields")
        fun shouldUpdateMemberFields() {
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
        @DisplayName("Should update member status")
        fun shouldUpdateMemberStatus() {
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
        @DisplayName("Should update updatedAt timestamp on modification")
        fun shouldUpdateTimestamp() {
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
}

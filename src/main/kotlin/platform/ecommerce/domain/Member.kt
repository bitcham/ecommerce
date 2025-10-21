package platform.ecommerce.domain

import jakarta.persistence.*
import platform.ecommerce.domain.vo.Address
import platform.ecommerce.enums.MemberRole
import platform.ecommerce.enums.MemberStatus
import java.util.*

@Entity
@Table(name = "members")
class Member(


    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    var passwordHash: String,

    @Column(nullable = false)
    var firstName: String,

    @Column(nullable = false)
    var lastName: String,

    @Column
    var phone: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: MemberRole = MemberRole.CUSTOMER,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: MemberStatus = MemberStatus.PENDING,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "streetAddress", column = Column(name = "billing_street_address")),
        AttributeOverride(name = "city", column = Column(name = "billing_city")),
        AttributeOverride(name = "postalCode", column = Column(name = "billing_postal_code"))
    )
    var billingAddress: Address? = null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "streetAddress", column = Column(name = "delivery_street_address")),
        AttributeOverride(name = "city", column = Column(name = "delivery_city")),
        AttributeOverride(name = "postalCode", column = Column(name = "delivery_postal_code"))
    )
    var deliveryAddress: Address? = null,

): BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null
        protected set

}

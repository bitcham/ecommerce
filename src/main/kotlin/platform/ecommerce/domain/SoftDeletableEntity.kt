package platform.ecommerce.domain

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.SQLRestriction
import java.time.Instant

@MappedSuperclass
abstract class SoftDeletableEntity : VersionedBaseEntity() {

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
        protected set

    fun isDeleted(): Boolean = deletedAt != null

    fun softDelete() {
        require(!isDeleted()) { "Entity is already deleted" }
        deletedAt = Instant.now()
    }

    fun restore() {
        require(isDeleted()) { "Entity is not deleted" }
        deletedAt = null
    }
}

package platform.ecommerce.domain

import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Version

@MappedSuperclass
abstract class VersionedBaseEntity: BaseEntity() {
    @Version
    var version: Long = 0
        protected set
}
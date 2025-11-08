package platform.ecommerce.domain

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PreUpdate
import java.time.Instant

@MappedSuperclass
abstract class BaseEntity(
    @Column(nullable = false, name = "created_at", updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(nullable = false, name = "updated_at")
    var updatedAt: Instant = Instant.now()
) {
    @PreUpdate
    fun onPreUpdate() {
        updatedAt = Instant.now()
    }
}

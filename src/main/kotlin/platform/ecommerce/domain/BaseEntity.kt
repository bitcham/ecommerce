package platform.ecommerce.domain

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PreUpdate
import java.time.LocalDateTime

@MappedSuperclass
abstract class BaseEntity(
    @Column(nullable = false, name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false, name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun onPreUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

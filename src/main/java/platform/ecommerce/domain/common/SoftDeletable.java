package platform.ecommerce.domain.common;

import java.time.LocalDateTime;

/**
 * Interface for entities that support soft delete.
 */
public interface SoftDeletable {

    LocalDateTime getDeletedAt();

    void delete();

    void restore();

    default boolean isDeleted() {
        return getDeletedAt() != null;
    }
}

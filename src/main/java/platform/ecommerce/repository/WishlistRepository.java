package platform.ecommerce.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import platform.ecommerce.domain.wishlist.Wishlist;

import java.util.Optional;

/**
 * Repository for Wishlist entity.
 */
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    Optional<Wishlist> findByMemberIdAndProductId(Long memberId, Long productId);

    boolean existsByMemberIdAndProductId(Long memberId, Long productId);

    Page<Wishlist> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    long countByMemberId(Long memberId);

    void deleteByMemberIdAndProductId(Long memberId, Long productId);
}

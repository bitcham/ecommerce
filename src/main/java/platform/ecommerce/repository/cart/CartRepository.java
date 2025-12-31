package platform.ecommerce.repository.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import platform.ecommerce.domain.cart.Cart;

import java.util.Optional;

/**
 * Repository for Cart aggregate root.
 */
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByMemberId(Long memberId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.memberId = :memberId")
    Optional<Cart> findByMemberIdWithItems(@Param("memberId") Long memberId);

    boolean existsByMemberId(Long memberId);
}

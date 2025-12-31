package platform.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import platform.ecommerce.domain.payment.Payment;

import java.util.List;
import java.util.Optional;

/**
 * Payment JPA repository.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by transaction ID.
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Find all payments for an order.
     */
    List<Payment> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    /**
     * Find latest payment for an order.
     */
    Optional<Payment> findFirstByOrderIdOrderByCreatedAtDesc(Long orderId);
}

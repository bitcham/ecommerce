package platform.ecommerce.service.payment;

import platform.ecommerce.domain.order.PaymentMethod;
import platform.ecommerce.domain.payment.Payment;

import java.math.BigDecimal;
import java.util.List;

/**
 * Domain service interface for Payment operations.
 * Returns Payment entities for ApplicationService to convert to DTOs.
 */
public interface PaymentService {

    /**
     * Request payment for an order.
     * @return created Payment entity
     */
    Payment requestPayment(Long orderId, PaymentMethod method);

    /**
     * Confirm payment after user authorization.
     * @return updated Payment entity
     */
    Payment confirmPayment(String transactionId, BigDecimal amount);

    /**
     * Cancel/refund a payment.
     * @param paymentId the payment ID
     * @param memberId the member ID for ownership validation
     * @return updated Payment entity
     */
    Payment cancelPayment(Long paymentId, Long memberId);

    /**
     * Get payment by ID.
     * Verifies the member owns the payment or is an admin.
     */
    Payment getPayment(Long paymentId, Long memberId);

    /**
     * Get payment history for an order.
     * Verifies the member owns the order or is an admin.
     */
    List<Payment> getPaymentsByOrderId(Long orderId, Long memberId);
}

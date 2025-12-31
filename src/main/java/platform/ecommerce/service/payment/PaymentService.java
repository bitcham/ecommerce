package platform.ecommerce.service.payment;

import platform.ecommerce.domain.order.PaymentMethod;
import platform.ecommerce.dto.response.payment.PaymentResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for Payment operations.
 */
public interface PaymentService {

    /**
     * Request payment for an order.
     */
    PaymentResponse requestPayment(Long orderId, PaymentMethod method);

    /**
     * Confirm payment after user authorization.
     */
    PaymentResponse confirmPayment(String transactionId, BigDecimal amount);

    /**
     * Cancel/refund a payment.
     * @param paymentId the payment ID
     * @param memberId the member ID for ownership validation
     */
    PaymentResponse cancelPayment(Long paymentId, Long memberId);

    /**
     * Get payment by ID.
     * Verifies the member owns the payment or is an admin.
     */
    PaymentResponse getPayment(Long paymentId, Long memberId);

    /**
     * Get payment history for an order.
     * Verifies the member owns the order or is an admin.
     */
    List<PaymentResponse> getPaymentsByOrderId(Long orderId, Long memberId);
}

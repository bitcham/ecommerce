package platform.ecommerce.service.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import platform.ecommerce.domain.order.PaymentMethod;
import platform.ecommerce.domain.payment.Payment;
import platform.ecommerce.dto.response.payment.PaymentResponse;
import platform.ecommerce.mapper.PaymentMapper;
import platform.ecommerce.service.payment.PaymentService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Payment application service.
 * Handles DTO conversion using PaymentMapper.
 */
@Service
@RequiredArgsConstructor
public class PaymentApplicationService {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    /**
     * Request payment for an order.
     */
    public PaymentResponse requestPayment(Long orderId, PaymentMethod method) {
        Payment payment = paymentService.requestPayment(orderId, method);
        return paymentMapper.toResponse(payment);
    }

    /**
     * Confirm payment after user authorization.
     */
    public PaymentResponse confirmPayment(String transactionId, BigDecimal amount) {
        Payment payment = paymentService.confirmPayment(transactionId, amount);
        return paymentMapper.toResponse(payment);
    }

    /**
     * Cancel/refund a payment.
     */
    public PaymentResponse cancelPayment(Long paymentId, Long memberId) {
        Payment payment = paymentService.cancelPayment(paymentId, memberId);
        return paymentMapper.toResponse(payment);
    }

    /**
     * Get payment by ID.
     */
    public PaymentResponse getPayment(Long paymentId, Long memberId) {
        Payment payment = paymentService.getPayment(paymentId, memberId);
        return paymentMapper.toResponse(payment);
    }

    /**
     * Get payment history for an order.
     */
    public List<PaymentResponse> getPaymentsByOrderId(Long orderId, Long memberId) {
        List<Payment> payments = paymentService.getPaymentsByOrderId(orderId, memberId);
        return payments.stream()
                .map(paymentMapper::toResponse)
                .toList();
    }
}

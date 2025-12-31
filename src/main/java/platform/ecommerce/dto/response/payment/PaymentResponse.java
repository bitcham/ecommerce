package platform.ecommerce.dto.response.payment;

import lombok.Builder;
import platform.ecommerce.domain.order.PaymentMethod;
import platform.ecommerce.domain.payment.Payment;
import platform.ecommerce.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment response DTO.
 */
@Builder
public record PaymentResponse(
        Long id,
        Long orderId,
        PaymentMethod method,
        PaymentStatus status,
        BigDecimal amount,
        String transactionId,
        String pgTransactionId,
        String failReason,
        LocalDateTime paidAt,
        LocalDateTime cancelledAt,
        LocalDateTime createdAt
) {
    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .transactionId(payment.getTransactionId())
                .pgTransactionId(payment.getPgTransactionId())
                .failReason(payment.getFailReason())
                .paidAt(payment.getPaidAt())
                .cancelledAt(payment.getCancelledAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}

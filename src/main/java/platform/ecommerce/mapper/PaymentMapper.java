package platform.ecommerce.mapper;

import org.mapstruct.Mapper;
import platform.ecommerce.domain.payment.Payment;
import platform.ecommerce.dto.response.payment.PaymentResponse;

/**
 * MapStruct mapper for Payment-related DTOs.
 */
@Mapper(componentModel = "spring")
public interface PaymentMapper {

    default PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }
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

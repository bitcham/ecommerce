package platform.ecommerce.domain.payment;

import jakarta.persistence.*;
import lombok.*;
import platform.ecommerce.domain.common.BaseEntity;
import platform.ecommerce.domain.order.PaymentMethod;
import platform.ecommerce.exception.ErrorCode;
import platform.ecommerce.exception.InvalidStateException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment entity for tracking payment history.
 */
@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_id", nullable = false, unique = true, length = 100)
    private String transactionId;

    @Column(name = "pg_transaction_id", length = 100)
    private String pgTransactionId;

    @Column(name = "fail_reason", length = 500)
    private String failReason;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Builder
    public Payment(Long orderId, PaymentMethod method, BigDecimal amount) {
        this.orderId = orderId;
        this.method = method;
        this.amount = amount;
        this.transactionId = generateTransactionId();
        this.status = PaymentStatus.PENDING;
    }

    /**
     * Complete payment with PG transaction ID.
     */
    public void complete(String pgTransactionId) {
        if (!this.status.canConfirm()) {
            throw new InvalidStateException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
        }
        this.status = PaymentStatus.COMPLETED;
        this.pgTransactionId = pgTransactionId;
        this.paidAt = LocalDateTime.now();
    }

    /**
     * Mark payment as failed.
     */
    public void fail(String reason) {
        if (!this.status.canConfirm()) {
            throw new InvalidStateException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
        }
        this.status = PaymentStatus.FAILED;
        this.failReason = reason;
    }

    /**
     * Cancel payment (refund).
     */
    public void cancel() {
        if (!this.status.canCancel()) {
            throw new InvalidStateException(ErrorCode.PAYMENT_CANNOT_CANCEL);
        }
        this.status = PaymentStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    private String generateTransactionId() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
}

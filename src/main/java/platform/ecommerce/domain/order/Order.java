package platform.ecommerce.domain.order;

import jakarta.persistence.*;
import lombok.*;
import platform.ecommerce.domain.common.BaseEntity;
import platform.ecommerce.exception.ErrorCode;
import platform.ecommerce.exception.InvalidStateException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Order aggregate root.
 * Manages order lifecycle, items, and payment.
 */
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Embedded
    private ShippingAddress shippingAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_transaction_id", length = 100)
    private String paymentTransactionId;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "shipping_fee", precision = 12, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Builder
    public Order(Long memberId, ShippingAddress shippingAddress, BigDecimal shippingFee, BigDecimal discountAmount) {
        this.orderNumber = generateOrderNumber();
        this.memberId = memberId;
        this.shippingAddress = shippingAddress;
        this.shippingFee = shippingFee != null ? shippingFee : BigDecimal.ZERO;
        this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.status = OrderStatus.PENDING_PAYMENT;
    }

    // ========== Order Items ==========

    /**
     * Add item to order.
     */
    public OrderItem addItem(Long productId, Long productOptionId, String productName,
                             String optionName, BigDecimal unitPrice, int quantity) {
        OrderItem item = OrderItem.builder()
                .order(this)
                .productId(productId)
                .productOptionId(productOptionId)
                .productName(productName)
                .optionName(optionName)
                .unitPrice(unitPrice)
                .quantity(quantity)
                .build();

        this.items.add(item);
        recalculateTotalAmount();
        return item;
    }

    /**
     * Calculate order subtotal (sum of item subtotals).
     */
    public BigDecimal getSubtotal() {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get total order amount.
     */
    public BigDecimal getTotalAmount() {
        return this.totalAmount != null ? this.totalAmount : calculateTotalAmount();
    }

    /**
     * Calculate total order amount.
     */
    private BigDecimal calculateTotalAmount() {
        return getSubtotal()
                .add(shippingFee)
                .subtract(discountAmount);
    }

    /**
     * Recalculate and update total amount.
     */
    private void recalculateTotalAmount() {
        this.totalAmount = calculateTotalAmount();
    }

    // ========== Status Transitions ==========

    /**
     * Mark order as paid.
     */
    public void markAsPaid(PaymentMethod paymentMethod, String transactionId) {
        validateTransition(OrderStatus.PAID);
        this.status = OrderStatus.PAID;
        this.paymentMethod = paymentMethod;
        this.paymentTransactionId = transactionId;
        this.paidAt = LocalDateTime.now();
    }

    /**
     * Start preparing order.
     */
    public void startPreparing() {
        validateTransition(OrderStatus.PREPARING);
        this.status = OrderStatus.PREPARING;
    }

    /**
     * Ship order with tracking number.
     */
    public void ship(String trackingNumber) {
        validateTransition(OrderStatus.SHIPPED);
        this.status = OrderStatus.SHIPPED;
        this.trackingNumber = trackingNumber;
        this.shippedAt = LocalDateTime.now();
        this.items.forEach(OrderItem::ship);
    }

    /**
     * Mark order as delivered.
     */
    public void deliver() {
        validateTransition(OrderStatus.DELIVERED);
        this.status = OrderStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        this.items.forEach(OrderItem::deliver);
    }

    /**
     * Cancel order.
     */
    public void cancel(String reason) {
        if (!this.status.canCancel()) {
            throw new InvalidStateException(ErrorCode.ORDER_CANNOT_CANCEL);
        }
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancelReason = reason;
        this.items.forEach(OrderItem::cancel);
    }

    // ========== Validation ==========

    private void validateTransition(OrderStatus nextStatus) {
        if (!this.status.canTransitionTo(nextStatus)) {
            throw new InvalidStateException(ErrorCode.ORDER_STATUS_INVALID,
                    String.format("Cannot transition from %s to %s", this.status, nextStatus));
        }
    }

    /**
     * Validate order has items before placing.
     */
    public void validateForPlacement() {
        if (this.items.isEmpty()) {
            throw new InvalidStateException(ErrorCode.INVALID_INPUT, "Order must have at least one item");
        }
    }

    // ========== Helper ==========

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Check if order is in final state.
     */
    public boolean isCompleted() {
        return this.status.isCompleted();
    }
}

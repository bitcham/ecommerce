package platform.ecommerce.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import platform.ecommerce.exception.InvalidStateException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Order domain unit tests.
 */
@DisplayName("Order Domain Tests")
class OrderTest {

    @Nested
    @DisplayName("Order Creation")
    class OrderCreation {

        @Test
        @DisplayName("Should create order with valid data")
        void create_withValidData_shouldSucceed() {
            // when
            Order order = createOrder();

            // then
            assertThat(order.getOrderNumber()).startsWith("ORD-");
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
            assertThat(order.getMemberId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception for empty items on validation")
        void validateForPlacement_emptyItems_shouldThrowException() {
            // given
            Order order = createOrder();

            // when & then
            assertThatThrownBy(order::validateForPlacement)
                    .isInstanceOf(InvalidStateException.class);
        }

        @Test
        @DisplayName("Should calculate total correctly")
        void getTotalAmount_multipleItems_shouldCalculateCorrectly() {
            // given
            Order order = createOrder();
            order.addItem(1L, 1L, "Product A", "Size M", new BigDecimal("10000"), 2);
            order.addItem(2L, 2L, "Product B", "Color Red", new BigDecimal("5000"), 3);

            // when
            BigDecimal total = order.getTotalAmount();

            // then
            // (10000 * 2) + (5000 * 3) + 3000 shipping - 1000 discount = 37000
            assertThat(total).isEqualByComparingTo("37000");
        }
    }

    @Nested
    @DisplayName("Status Transitions")
    class StatusTransitions {

        @Test
        @DisplayName("Should transition PENDING_PAYMENT to PAID")
        void markAsPaid_fromPending_shouldSucceed() {
            // given
            Order order = createOrderWithItems();

            // when
            order.markAsPaid(PaymentMethod.CREDIT_CARD, "TXN-123");

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(order.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
            assertThat(order.getPaidAt()).isNotNull();
        }

        @Test
        @DisplayName("Should transition PAID to PREPARING")
        void startPreparing_fromPaid_shouldSucceed() {
            // given
            Order order = createPaidOrder();

            // when
            order.startPreparing();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PREPARING);
        }

        @Test
        @DisplayName("Should transition PREPARING to SHIPPED")
        void ship_fromPreparing_shouldSucceed() {
            // given
            Order order = createPreparingOrder();

            // when
            order.ship("TRACK-123");

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
            assertThat(order.getTrackingNumber()).isEqualTo("TRACK-123");
            assertThat(order.getShippedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should transition SHIPPED to DELIVERED")
        void deliver_fromShipped_shouldSucceed() {
            // given
            Order order = createShippedOrder();

            // when
            order.deliver();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
            assertThat(order.getDeliveredAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception for invalid transition")
        void markAsPaid_fromDelivered_shouldThrowException() {
            // given
            Order order = createDeliveredOrder();

            // when & then
            assertThatThrownBy(() -> order.markAsPaid(PaymentMethod.CREDIT_CARD, "TXN"))
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    @Nested
    @DisplayName("Cancellation")
    class Cancellation {

        @Test
        @DisplayName("Should cancel PENDING_PAYMENT order")
        void cancel_pendingOrder_shouldSucceed() {
            // given
            Order order = createOrderWithItems();

            // when
            order.cancel("Customer request");

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(order.getCancelReason()).isEqualTo("Customer request");
            assertThat(order.getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("Should cancel PAID order")
        void cancel_paidOrder_shouldSucceed() {
            // given
            Order order = createPaidOrder();

            // when
            order.cancel("Out of stock");

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should throw exception for SHIPPED order")
        void cancel_shippedOrder_shouldThrowException() {
            // given
            Order order = createShippedOrder();

            // when & then
            assertThatThrownBy(() -> order.cancel("Too late"))
                    .isInstanceOf(InvalidStateException.class);
        }

        @Test
        @DisplayName("Should throw exception for DELIVERED order")
        void cancel_deliveredOrder_shouldThrowException() {
            // given
            Order order = createDeliveredOrder();

            // when & then
            assertThatThrownBy(() -> order.cancel("Change mind"))
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    @Nested
    @DisplayName("Order Items")
    class OrderItems {

        @Test
        @DisplayName("Should add item with quantity")
        void addItem_shouldSucceed() {
            // given
            Order order = createOrder();

            // when
            OrderItem item = order.addItem(1L, 1L, "Product", "Option", new BigDecimal("10000"), 2);

            // then
            assertThat(order.getItems()).hasSize(1);
            assertThat(item.getQuantity()).isEqualTo(2);
            assertThat(item.getStatus()).isEqualTo(OrderItemStatus.ORDERED);
        }

        @Test
        @DisplayName("Should calculate item subtotal")
        void getSubtotal_shouldCalculateCorrectly() {
            // given
            Order order = createOrder();
            OrderItem item = order.addItem(1L, 1L, "Product", "Option", new BigDecimal("10000"), 3);

            // when
            BigDecimal subtotal = item.getSubtotal();

            // then
            assertThat(subtotal).isEqualByComparingTo("30000");
        }

        @Test
        @DisplayName("Should throw exception for invalid quantity")
        void addItem_zeroQuantity_shouldThrowException() {
            // given
            Order order = createOrder();

            // when & then
            assertThatThrownBy(() -> order.addItem(1L, 1L, "Product", "Option", new BigDecimal("10000"), 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ========== Helper Methods ==========

    private Order createOrder() {
        return Order.builder()
                .memberId(1L)
                .shippingAddress(createShippingAddress())
                .shippingFee(new BigDecimal("3000"))
                .discountAmount(new BigDecimal("1000"))
                .build();
    }

    private Order createOrderWithItems() {
        Order order = createOrder();
        order.addItem(1L, 1L, "Product A", "Size M", new BigDecimal("10000"), 1);
        return order;
    }

    private Order createPaidOrder() {
        Order order = createOrderWithItems();
        order.markAsPaid(PaymentMethod.CREDIT_CARD, "TXN-123");
        return order;
    }

    private Order createPreparingOrder() {
        Order order = createPaidOrder();
        order.startPreparing();
        return order;
    }

    private Order createShippedOrder() {
        Order order = createPreparingOrder();
        order.ship("TRACK-123");
        return order;
    }

    private Order createDeliveredOrder() {
        Order order = createShippedOrder();
        order.deliver();
        return order;
    }

    private ShippingAddress createShippingAddress() {
        return ShippingAddress.builder()
                .recipientName("John Doe")
                .recipientPhone("010-1234-5678")
                .zipCode("12345")
                .address("123 Main St")
                .addressDetail("Apt 101")
                .build();
    }
}

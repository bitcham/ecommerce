package platform.ecommerce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import platform.ecommerce.domain.order.*;
import platform.ecommerce.dto.request.order.*;
import platform.ecommerce.dto.response.order.OrderResponse;
import platform.ecommerce.exception.EntityNotFoundException;
import platform.ecommerce.exception.InvalidStateException;
import platform.ecommerce.repository.MemberRepository;
import platform.ecommerce.repository.order.OrderRepository;
import platform.ecommerce.service.email.EmailService;
import platform.ecommerce.service.notification.NotificationService;
import platform.ecommerce.service.order.OrderServiceImpl;
import platform.ecommerce.service.product.ProductService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for OrderService.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private OrderCreateRequest createRequest;
    private static final Long MEMBER_ID = 1L;
    private static final Long ORDER_ID = 100L;

    @BeforeEach
    void setUp() {
        ShippingAddress shippingAddress = ShippingAddress.builder()
                .recipientName("John Doe")
                .recipientPhone("010-1234-5678")
                .zipCode("12345")
                .address("Seoul, Korea")
                .addressDetail("Apt 101")
                .build();

        testOrder = Order.builder()
                .memberId(MEMBER_ID)
                .shippingAddress(shippingAddress)
                .shippingFee(BigDecimal.valueOf(3000))
                .discountAmount(BigDecimal.ZERO)
                .build();
        ReflectionTestUtils.setField(testOrder, "id", ORDER_ID);

        ShippingAddressRequest addressRequest = ShippingAddressRequest.builder()
                .recipientName("John Doe")
                .recipientPhone("010-1234-5678")
                .zipCode("12345")
                .address("Seoul, Korea")
                .addressDetail("Apt 101")
                .build();

        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(1L)
                .productOptionId(10L)
                .productName("Test Product")
                .optionName("Size M")
                .unitPrice(BigDecimal.valueOf(29000))
                .quantity(2)
                .build();

        createRequest = OrderCreateRequest.builder()
                .shippingAddress(addressRequest)
                .items(List.of(itemRequest))
                .shippingFee(BigDecimal.valueOf(3000))
                .discountAmount(BigDecimal.ZERO)
                .build();
    }

    @Nested
    @DisplayName("createOrder")
    class CreateOrder {

        @Test
        @DisplayName("should create order with items and decrease stock")
        void createOrderSuccessfully() {
            // given
            given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                ReflectionTestUtils.setField(order, "id", ORDER_ID);
                return order;
            });

            // when
            OrderResponse response = orderService.createOrder(MEMBER_ID, createRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.memberId()).isEqualTo(MEMBER_ID);
            assertThat(response.status()).isEqualTo(OrderStatus.PENDING_PAYMENT);
            assertThat(response.shippingAddress().recipientName()).isEqualTo("John Doe");
            assertThat(response.items()).hasSize(1);
            assertThat(response.items().get(0).productName()).isEqualTo("Test Product");

            // verify stock was decreased
            verify(productService).decreaseStock(1L, 10L, 2);
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("should calculate totals correctly")
        void createOrderWithCorrectTotals() {
            // given
            given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                ReflectionTestUtils.setField(order, "id", ORDER_ID);
                return order;
            });

            // when
            OrderResponse response = orderService.createOrder(MEMBER_ID, createRequest);

            // then
            // 29000 * 2 = 58000 subtotal
            assertThat(response.subtotal()).isEqualByComparingTo(BigDecimal.valueOf(58000));
            assertThat(response.shippingFee()).isEqualByComparingTo(BigDecimal.valueOf(3000));
            assertThat(response.discountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            // total = 58000 + 3000 - 0 = 61000
            assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(61000));
        }
    }

    @Nested
    @DisplayName("getOrder")
    class GetOrder {

        @Test
        @DisplayName("should return order by id for owner")
        void getOrderById() {
            // given
            testOrder.addItem(1L, 10L, "Test Product", "Size M", BigDecimal.valueOf(29000), 2);
            given(orderRepository.findByIdWithItems(ORDER_ID)).willReturn(Optional.of(testOrder));

            // when
            OrderResponse response = orderService.getOrder(ORDER_ID, MEMBER_ID);

            // then
            assertThat(response.id()).isEqualTo(ORDER_ID);
            assertThat(response.memberId()).isEqualTo(MEMBER_ID);
        }

        @Test
        @DisplayName("should throw exception when order not found")
        void getOrderNotFound() {
            // given
            given(orderRepository.findByIdWithItems(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.getOrder(999L, MEMBER_ID))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("should throw exception when not owner")
        void getOrderNotOwner() {
            // given
            testOrder.addItem(1L, 10L, "Test Product", "Size M", BigDecimal.valueOf(29000), 2);
            given(orderRepository.findByIdWithItems(ORDER_ID)).willReturn(Optional.of(testOrder));

            // when & then
            assertThatThrownBy(() -> orderService.getOrder(ORDER_ID, 999L))
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    @Nested
    @DisplayName("getOrderByNumber")
    class GetOrderByNumber {

        @Test
        @DisplayName("should return order by order number for owner")
        void getOrderByOrderNumber() {
            // given
            given(orderRepository.findByOrderNumberWithItems("ORD-12345678"))
                    .willReturn(Optional.of(testOrder));

            // when
            OrderResponse response = orderService.getOrderByNumber("ORD-12345678", MEMBER_ID);

            // then
            assertThat(response.id()).isEqualTo(ORDER_ID);
        }

        @Test
        @DisplayName("should throw exception when order number not found")
        void getOrderByNumberNotFound() {
            // given
            given(orderRepository.findByOrderNumberWithItems("ORD-INVALID"))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.getOrderByNumber("ORD-INVALID", MEMBER_ID))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getMyOrders")
    class GetMyOrders {

        @Test
        @DisplayName("should return paginated orders for member")
        void getMyOrdersPaginated() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> orderPage = new PageImpl<>(List.of(testOrder), pageable, 1);
            given(orderRepository.search(any(OrderSearchCondition.class), eq(pageable)))
                    .willReturn(orderPage);

            // when
            Page<OrderResponse> response = orderService.getMyOrders(MEMBER_ID, pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getTotalElements()).isEqualTo(1);

            // verify correct condition was passed
            ArgumentCaptor<OrderSearchCondition> conditionCaptor =
                    ArgumentCaptor.forClass(OrderSearchCondition.class);
            verify(orderRepository).search(conditionCaptor.capture(), eq(pageable));
            assertThat(conditionCaptor.getValue().memberId()).isEqualTo(MEMBER_ID);
        }
    }

    @Nested
    @DisplayName("processPayment")
    class ProcessPayment {

        @Test
        @DisplayName("should process payment for pending order")
        void processPaymentSuccessfully() {
            // given
            given(orderRepository.findByIdWithItems(ORDER_ID)).willReturn(Optional.of(testOrder));

            // when
            OrderResponse response = orderService.processPayment(
                    ORDER_ID, PaymentMethod.CREDIT_CARD, "TXN-123456"
            );

            // then
            assertThat(response.status()).isEqualTo(OrderStatus.PAID);
            assertThat(response.paymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
            assertThat(response.paidAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw exception when order already paid")
        void processPaymentAlreadyPaid() {
            // given
            testOrder.markAsPaid(PaymentMethod.CREDIT_CARD, "TXN-OLD");
            given(orderRepository.findByIdWithItems(ORDER_ID)).willReturn(Optional.of(testOrder));

            // when & then
            assertThatThrownBy(() ->
                    orderService.processPayment(ORDER_ID, PaymentMethod.BANK_TRANSFER, "TXN-NEW"))
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    @Nested
    @DisplayName("startPreparing")
    class StartPreparing {

        @Test
        @DisplayName("should start preparing paid order")
        void startPreparingSuccessfully() {
            // given
            testOrder.markAsPaid(PaymentMethod.CREDIT_CARD, "TXN-123");
            given(orderRepository.findByIdWithItems(ORDER_ID)).willReturn(Optional.of(testOrder));

            // when
            OrderResponse response = orderService.startPreparing(ORDER_ID);

            // then
            assertThat(response.status()).isEqualTo(OrderStatus.PREPARING);
        }

        @Test
        @DisplayName("should throw exception when order not paid")
        void startPreparingNotPaid() {
            // given
            given(orderRepository.findByIdWithItems(ORDER_ID)).willReturn(Optional.of(testOrder));

            // when & then
            assertThatThrownBy(() -> orderService.startPreparing(ORDER_ID))
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    @Nested
    @DisplayName("shipOrder")
    class ShipOrder {

        @Test
        @DisplayName("should ship preparing order with tracking number")
        void shipOrderSuccessfully() {
            // given
            testOrder.markAsPaid(PaymentMethod.CREDIT_CARD, "TXN-123");
            testOrder.startPreparing();
            given(orderRepository.findByIdWithItems(ORDER_ID)).willReturn(Optional.of(testOrder));

            // when
            OrderResponse response = orderService.shipOrder(ORDER_ID, "TRACK-123456");

            // then
            assertThat(response.status()).isEqualTo(OrderStatus.SHIPPED);
            assertThat(response.trackingNumber()).isEqualTo("TRACK-123456");
            assertThat(response.shippedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("deliverOrder")
    class DeliverOrder {

        @Test
        @DisplayName("should deliver shipped order")
        void deliverOrderSuccessfully() {
            // given
            testOrder.markAsPaid(PaymentMethod.CREDIT_CARD, "TXN-123");
            testOrder.startPreparing();
            testOrder.ship("TRACK-123456");
            given(orderRepository.findByIdWithItems(ORDER_ID)).willReturn(Optional.of(testOrder));

            // when
            OrderResponse response = orderService.deliverOrder(ORDER_ID);

            // then
            assertThat(response.status()).isEqualTo(OrderStatus.DELIVERED);
            assertThat(response.deliveredAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("cancelOrder")
    class CancelOrder {

        @Test
        @DisplayName("should cancel pending payment order and restore stock")
        void cancelOrderSuccessfully() {
            // given
            testOrder.addItem(1L, 10L, "Test Product", "Size M", BigDecimal.valueOf(29000), 2);
            given(orderRepository.findByIdWithItems(ORDER_ID)).willReturn(Optional.of(testOrder));

            // when
            OrderResponse response = orderService.cancelOrder(ORDER_ID, MEMBER_ID, "Customer request");

            // then
            assertThat(response.status()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(response.cancelledAt()).isNotNull();

            // verify stock was restored
            verify(productService).increaseStock(1L, 10L, 2);
        }

        @Test
        @DisplayName("should throw exception when order cannot be cancelled")
        void cancelOrderNotAllowed() {
            // given
            testOrder.markAsPaid(PaymentMethod.CREDIT_CARD, "TXN-123");
            testOrder.startPreparing();
            testOrder.ship("TRACK-123");
            testOrder.deliver();
            given(orderRepository.findByIdWithItems(ORDER_ID)).willReturn(Optional.of(testOrder));

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(ORDER_ID, MEMBER_ID, "Too late"))
                    .isInstanceOf(InvalidStateException.class);
        }

        @Test
        @DisplayName("should throw exception when not owner")
        void cancelOrderNotOwner() {
            // given
            testOrder.addItem(1L, 10L, "Test Product", "Size M", BigDecimal.valueOf(29000), 2);
            given(orderRepository.findByIdWithItems(ORDER_ID)).willReturn(Optional.of(testOrder));

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(ORDER_ID, 999L, "Unauthorized attempt"))
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    @Nested
    @DisplayName("cancelOrderItem")
    class CancelOrderItem {

        @Test
        @DisplayName("should cancel specific item and restore stock")
        void cancelOrderItemSuccessfully() {
            // given
            OrderItem item = testOrder.addItem(1L, 10L, "Test Product", "Size M",
                    BigDecimal.valueOf(29000), 2);
            ReflectionTestUtils.setField(item, "id", 50L);
            given(orderRepository.findByIdWithItems(ORDER_ID)).willReturn(Optional.of(testOrder));

            // when
            OrderResponse response = orderService.cancelOrderItem(ORDER_ID, MEMBER_ID, 50L, "Wrong item");

            // then
            assertThat(response.items().get(0).status()).isEqualTo(OrderItemStatus.CANCELLED);
            verify(productService).increaseStock(1L, 10L, 2);
        }

        @Test
        @DisplayName("should throw exception when item not found")
        void cancelOrderItemNotFound() {
            // given
            given(orderRepository.findByIdWithItems(ORDER_ID)).willReturn(Optional.of(testOrder));

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrderItem(ORDER_ID, MEMBER_ID, 999L, "Not found"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("searchOrders")
    class SearchOrders {

        @Test
        @DisplayName("should search orders with conditions")
        void searchOrdersWithConditions() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            OrderSearchCondition condition = new OrderSearchCondition(
                    MEMBER_ID, OrderStatus.PAID, null, null, null
            );
            Page<Order> orderPage = new PageImpl<>(List.of(testOrder), pageable, 1);
            given(orderRepository.search(condition, pageable)).willReturn(orderPage);

            // when
            Page<OrderResponse> response = orderService.searchOrders(condition, pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
            verify(orderRepository).search(condition, pageable);
        }
    }
}

package platform.ecommerce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import platform.ecommerce.domain.order.*;
import platform.ecommerce.domain.payment.Payment;
import platform.ecommerce.domain.payment.PaymentStatus;
import platform.ecommerce.exception.EntityNotFoundException;
import platform.ecommerce.exception.InvalidStateException;
import platform.ecommerce.repository.PaymentRepository;
import platform.ecommerce.repository.order.OrderRepository;
import platform.ecommerce.service.payment.PaymentGateway;
import platform.ecommerce.service.payment.PaymentResult;
import platform.ecommerce.service.payment.PaymentServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for PaymentService.
 * Tests pure business logic with Entity returns.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Order testOrder;
    private Payment testPayment;
    private static final Long MEMBER_ID = 1L;
    private static final Long ORDER_ID = 100L;
    private static final Long PAYMENT_ID = 200L;
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(100000);

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
                .memberId(1L)
                .shippingAddress(shippingAddress)
                .shippingFee(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .build();
        ReflectionTestUtils.setField(testOrder, "id", ORDER_ID);
        testOrder.addItem(1L, 10L, "Test Product", "Size M", AMOUNT, 1);

        testPayment = Payment.builder()
                .orderId(ORDER_ID)
                .method(PaymentMethod.CREDIT_CARD)
                .amount(AMOUNT)
                .build();
        ReflectionTestUtils.setField(testPayment, "id", PAYMENT_ID);
    }

    // ========== 1. Payment Request Tests ==========

    @Nested
    @DisplayName("requestPayment")
    class RequestPayment {

        @Test
        @DisplayName("should create payment with PENDING status for valid order")
        void requestPayment_success() {
            // given
            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(testOrder));
            given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> {
                Payment payment = invocation.getArgument(0);
                ReflectionTestUtils.setField(payment, "id", PAYMENT_ID);
                return payment;
            });

            // when
            Payment result = paymentService.requestPayment(ORDER_ID, PaymentMethod.CREDIT_CARD);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo(ORDER_ID);
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(result.getMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
            assertThat(result.getAmount()).isEqualByComparingTo(AMOUNT);
            assertThat(result.getTransactionId()).startsWith("PAY-");

            verify(paymentRepository).save(any(Payment.class));
        }

        @Test
        @DisplayName("should throw exception when order not in PENDING_PAYMENT status")
        void requestPayment_invalidOrderStatus() {
            // given
            testOrder.markAsPaid(PaymentMethod.CREDIT_CARD, "TXN-OLD");
            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(testOrder));

            // when & then
            assertThatThrownBy(() -> paymentService.requestPayment(ORDER_ID, PaymentMethod.CREDIT_CARD))
                    .isInstanceOf(InvalidStateException.class);
        }

        @Test
        @DisplayName("should throw exception when order not found")
        void requestPayment_orderNotFound() {
            // given
            given(orderRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentService.requestPayment(999L, PaymentMethod.CREDIT_CARD))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // ========== 2. Payment Confirmation Tests ==========

    @Nested
    @DisplayName("confirmPayment")
    class ConfirmPayment {

        @Test
        @DisplayName("should complete payment and update order status")
        void confirmPayment_success() {
            // given
            String transactionId = testPayment.getTransactionId();
            given(paymentRepository.findByTransactionId(transactionId)).willReturn(Optional.of(testPayment));
            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(testOrder));
            given(paymentGateway.confirmPayment(eq(transactionId), eq(AMOUNT)))
                    .willReturn(PaymentResult.success("PG-12345678"));

            // when
            Payment result = paymentService.confirmPayment(transactionId, AMOUNT);

            // then
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(result.getPgTransactionId()).isEqualTo("PG-12345678");
            assertThat(result.getPaidAt()).isNotNull();

            // verify order was marked as paid
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.PAID);
        }

        @Test
        @DisplayName("should fail payment when gateway returns failure")
        void confirmPayment_gatewayFailure() {
            // given
            String transactionId = testPayment.getTransactionId();
            given(paymentRepository.findByTransactionId(transactionId)).willReturn(Optional.of(testPayment));
            given(paymentGateway.confirmPayment(eq(transactionId), eq(AMOUNT)))
                    .willReturn(PaymentResult.failure("Card declined"));

            // when
            Payment result = paymentService.confirmPayment(transactionId, AMOUNT);

            // then
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(result.getFailReason()).isEqualTo("Card declined");
        }

        @Test
        @DisplayName("should throw exception when payment already completed")
        void confirmPayment_alreadyCompleted() {
            // given
            testPayment.complete("PG-OLD");
            String transactionId = testPayment.getTransactionId();
            given(paymentRepository.findByTransactionId(transactionId)).willReturn(Optional.of(testPayment));

            // when & then
            assertThatThrownBy(() -> paymentService.confirmPayment(transactionId, AMOUNT))
                    .isInstanceOf(InvalidStateException.class);
        }

        @Test
        @DisplayName("should throw exception when amount mismatch")
        void confirmPayment_amountMismatch() {
            // given
            String transactionId = testPayment.getTransactionId();
            given(paymentRepository.findByTransactionId(transactionId)).willReturn(Optional.of(testPayment));

            // when & then
            assertThatThrownBy(() -> paymentService.confirmPayment(transactionId, BigDecimal.valueOf(50000)))
                    .isInstanceOf(InvalidStateException.class);
        }

        @Test
        @DisplayName("should throw exception when payment not found")
        void confirmPayment_notFound() {
            // given
            given(paymentRepository.findByTransactionId("INVALID")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentService.confirmPayment("INVALID", AMOUNT))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // ========== 3. Payment Cancellation Tests ==========

    @Nested
    @DisplayName("cancelPayment")
    class CancelPayment {

        private static final Long MEMBER_ID = 1L;

        @Test
        @DisplayName("should cancel completed payment")
        void cancelPayment_success() {
            // given
            testPayment.complete("PG-12345678");
            given(paymentRepository.findById(PAYMENT_ID)).willReturn(Optional.of(testPayment));
            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(testOrder));
            given(paymentGateway.cancelPayment(eq("PG-12345678"), eq(AMOUNT)))
                    .willReturn(PaymentResult.success("PG-12345678"));

            // when
            Payment result = paymentService.cancelPayment(PAYMENT_ID, MEMBER_ID);

            // then
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
            assertThat(result.getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw exception when payment not completed")
        void cancelPayment_notCompleted() {
            // given
            given(paymentRepository.findById(PAYMENT_ID)).willReturn(Optional.of(testPayment));
            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(testOrder));

            // when & then
            assertThatThrownBy(() -> paymentService.cancelPayment(PAYMENT_ID, MEMBER_ID))
                    .isInstanceOf(InvalidStateException.class);
        }

        @Test
        @DisplayName("should throw exception when payment not found")
        void cancelPayment_notFound() {
            // given
            given(paymentRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentService.cancelPayment(999L, MEMBER_ID))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("should throw exception when member is not owner")
        void cancelPayment_notOwner() {
            // given
            testPayment.complete("PG-12345678");
            given(paymentRepository.findById(PAYMENT_ID)).willReturn(Optional.of(testPayment));
            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(testOrder));

            // when & then
            assertThatThrownBy(() -> paymentService.cancelPayment(PAYMENT_ID, 999L))
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    // ========== 4. Payment Query Tests ==========

    @Nested
    @DisplayName("getPayment")
    class GetPayment {

        @Test
        @DisplayName("should return payment by id for owner")
        void getPayment_success() {
            // given
            given(paymentRepository.findById(PAYMENT_ID)).willReturn(Optional.of(testPayment));
            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(testOrder));

            // when
            Payment result = paymentService.getPayment(PAYMENT_ID, MEMBER_ID);

            // then
            assertThat(result.getId()).isEqualTo(PAYMENT_ID);
            assertThat(result.getOrderId()).isEqualTo(ORDER_ID);
        }

        @Test
        @DisplayName("should throw exception when payment not found")
        void getPayment_notFound() {
            // given
            given(paymentRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentService.getPayment(999L, MEMBER_ID))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("should throw exception when not owner")
        void getPayment_notOwner() {
            // given
            given(paymentRepository.findById(PAYMENT_ID)).willReturn(Optional.of(testPayment));
            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(testOrder));

            // when & then
            assertThatThrownBy(() -> paymentService.getPayment(PAYMENT_ID, 999L))
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    @Nested
    @DisplayName("getPaymentsByOrderId")
    class GetPaymentsByOrderId {

        @Test
        @DisplayName("should return payment history for order for owner")
        void getPaymentsByOrderId_success() {
            // given
            Payment payment2 = Payment.builder()
                    .orderId(ORDER_ID)
                    .method(PaymentMethod.BANK_TRANSFER)
                    .amount(AMOUNT)
                    .build();
            ReflectionTestUtils.setField(payment2, "id", 201L);

            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(testOrder));
            given(paymentRepository.findByOrderIdOrderByCreatedAtDesc(ORDER_ID))
                    .willReturn(List.of(testPayment, payment2));

            // when
            List<Payment> results = paymentService.getPaymentsByOrderId(ORDER_ID, MEMBER_ID);

            // then
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getOrderId()).isEqualTo(ORDER_ID);
            assertThat(results.get(1).getOrderId()).isEqualTo(ORDER_ID);
        }

        @Test
        @DisplayName("should return empty list when no payments")
        void getPaymentsByOrderId_empty() {
            // given
            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(testOrder));
            given(paymentRepository.findByOrderIdOrderByCreatedAtDesc(ORDER_ID)).willReturn(List.of());

            // when
            List<Payment> results = paymentService.getPaymentsByOrderId(ORDER_ID, MEMBER_ID);

            // then
            assertThat(results).isEmpty();
        }
    }
}

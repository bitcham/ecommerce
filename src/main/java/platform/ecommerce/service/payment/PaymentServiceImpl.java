package platform.ecommerce.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import platform.ecommerce.domain.order.Order;
import platform.ecommerce.domain.order.OrderStatus;
import platform.ecommerce.domain.order.PaymentMethod;
import platform.ecommerce.domain.payment.Payment;
import platform.ecommerce.dto.response.payment.PaymentResponse;
import platform.ecommerce.exception.EntityNotFoundException;
import platform.ecommerce.exception.ErrorCode;
import platform.ecommerce.exception.InvalidStateException;
import platform.ecommerce.repository.PaymentRepository;
import platform.ecommerce.repository.order.OrderRepository;
import platform.ecommerce.security.SecurityUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * Payment service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;

    @Override
    @Transactional
    public PaymentResponse requestPayment(Long orderId, PaymentMethod method) {
        log.info("Requesting payment for order: orderId={}, method={}", orderId, method);

        Order order = findOrderById(orderId);
        validateOrderForPayment(order);

        Payment payment = Payment.builder()
                .orderId(orderId)
                .method(method)
                .amount(order.getTotalAmount())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment created: transactionId={}", savedPayment.getTransactionId());

        return PaymentResponse.from(savedPayment);
    }

    @Override
    @Transactional
    public PaymentResponse confirmPayment(String transactionId, BigDecimal amount) {
        log.info("Confirming payment: transactionId={}, amount={}", transactionId, amount);

        Payment payment = findPaymentByTransactionId(transactionId);
        validatePaymentForConfirm(payment, amount);

        PaymentResult result = paymentGateway.confirmPayment(transactionId, amount);

        if (result.success()) {
            payment.complete(result.pgTransactionId());

            // Update order status to PAID
            Order order = findOrderById(payment.getOrderId());
            order.markAsPaid(payment.getMethod(), transactionId);

            log.info("Payment confirmed: transactionId={}, pgTransactionId={}",
                    transactionId, result.pgTransactionId());
        } else {
            payment.fail(result.failReason());
            log.warn("Payment failed: transactionId={}, reason={}", transactionId, result.failReason());
        }

        return PaymentResponse.from(payment);
    }

    @Override
    @Transactional
    public PaymentResponse cancelPayment(Long paymentId, Long memberId) {
        log.info("Cancelling payment: paymentId={}, memberId={}", paymentId, memberId);

        Payment payment = findPaymentById(paymentId);
        validatePaymentOwnership(payment, memberId);
        validatePaymentForCancel(payment);

        PaymentResult result = paymentGateway.cancelPayment(
                payment.getPgTransactionId(),
                payment.getAmount()
        );

        if (result.success()) {
            payment.cancel();
            log.info("Payment cancelled: paymentId={}", paymentId);
        } else {
            throw new InvalidStateException(ErrorCode.REFUND_FAILED, result.failReason());
        }

        return PaymentResponse.from(payment);
    }

    @Override
    public PaymentResponse getPayment(Long paymentId, Long memberId) {
        Payment payment = findPaymentById(paymentId);
        validatePaymentOwnershipOrAdmin(payment, memberId);
        return PaymentResponse.from(payment);
    }

    @Override
    public List<PaymentResponse> getPaymentsByOrderId(Long orderId, Long memberId) {
        Order order = findOrderById(orderId);
        validateOrderOwnershipOrAdmin(order, memberId);
        return paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }

    // ========== Private Helper Methods ==========

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));
    }

    private Payment findPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    private Payment findPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    private void validateOrderForPayment(Order order) {
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidStateException(ErrorCode.ORDER_ALREADY_PAID);
        }
    }

    private void validatePaymentForConfirm(Payment payment, BigDecimal amount) {
        if (!payment.getStatus().canConfirm()) {
            throw new InvalidStateException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
        }
        if (payment.getAmount().compareTo(amount) != 0) {
            throw new InvalidStateException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    private void validatePaymentForCancel(Payment payment) {
        if (!payment.getStatus().canCancel()) {
            throw new InvalidStateException(ErrorCode.PAYMENT_CANNOT_CANCEL);
        }
    }

    private void validatePaymentOwnership(Payment payment, Long memberId) {
        Order order = findOrderById(payment.getOrderId());
        if (!order.getMemberId().equals(memberId)) {
            throw new InvalidStateException(ErrorCode.FORBIDDEN, "Not authorized to cancel this payment");
        }
    }

    private void validatePaymentOwnershipOrAdmin(Payment payment, Long memberId) {
        if (SecurityUtils.hasRole("ADMIN")) {
            return;
        }
        Order order = findOrderById(payment.getOrderId());
        if (!order.getMemberId().equals(memberId)) {
            throw new InvalidStateException(ErrorCode.FORBIDDEN, "Not authorized to access this payment");
        }
    }

    private void validateOrderOwnershipOrAdmin(Order order, Long memberId) {
        if (SecurityUtils.hasRole("ADMIN")) {
            return;
        }
        if (!order.getMemberId().equals(memberId)) {
            throw new InvalidStateException(ErrorCode.FORBIDDEN, "Not authorized to access this order");
        }
    }
}

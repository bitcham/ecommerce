package platform.ecommerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import platform.ecommerce.dto.request.payment.PaymentConfirmRequest;
import platform.ecommerce.dto.request.payment.PaymentRequestDto;
import platform.ecommerce.dto.response.ApiResponse;
import platform.ecommerce.dto.response.payment.PaymentResponse;
import platform.ecommerce.security.SecurityUtils;
import platform.ecommerce.service.application.PaymentApplicationService;

import java.util.List;

/**
 * Payment REST controller.
 */
@Tag(name = "Payment", description = "Payment management API")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentApplicationService paymentApplicationService;

    @Operation(summary = "Request payment", description = "Request payment for an order")
    @PostMapping("/request")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PaymentResponse> requestPayment(
            @Valid @RequestBody PaymentRequestDto request
    ) {
        PaymentResponse response = paymentApplicationService.requestPayment(request.orderId(), request.method());
        return ApiResponse.created(response);
    }

    @Operation(summary = "Confirm payment", description = "Confirm payment after user authorization")
    @PostMapping("/confirm")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PaymentResponse> confirmPayment(
            @Valid @RequestBody PaymentConfirmRequest request
    ) {
        PaymentResponse response = paymentApplicationService.confirmPayment(
                request.transactionId(),
                request.amount()
        );
        return ApiResponse.success(response);
    }

    @Operation(summary = "Cancel payment", description = "Cancel/refund a payment")
    @PostMapping("/{paymentId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PaymentResponse> cancelPayment(
            @Parameter(description = "Payment ID") @PathVariable Long paymentId
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        PaymentResponse response = paymentApplicationService.cancelPayment(paymentId, memberId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Get payment", description = "Get payment by ID (owner or admin only)")
    @GetMapping("/{paymentId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PaymentResponse> getPayment(
            @Parameter(description = "Payment ID") @PathVariable Long paymentId
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        PaymentResponse response = paymentApplicationService.getPayment(paymentId, memberId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Get payments by order", description = "Get payment history for an order (owner or admin only)")
    @GetMapping("/orders/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<PaymentResponse>> getPaymentsByOrderId(
            @Parameter(description = "Order ID") @PathVariable Long orderId
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        List<PaymentResponse> responses = paymentApplicationService.getPaymentsByOrderId(orderId, memberId);
        return ApiResponse.success(responses);
    }
}

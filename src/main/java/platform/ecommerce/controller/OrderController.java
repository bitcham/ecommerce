package platform.ecommerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import platform.ecommerce.domain.order.OrderStatus;
import platform.ecommerce.domain.order.PaymentMethod;
import platform.ecommerce.dto.request.order.*;
import platform.ecommerce.dto.response.*;
import platform.ecommerce.dto.response.order.*;
import platform.ecommerce.security.SecurityUtils;
import platform.ecommerce.service.order.OrderService;

import java.time.LocalDateTime;

/**
 * Order REST controller.
 */
@Tag(name = "Order", description = "Order management API")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create order", description = "Create a new order")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<OrderResponse> createOrder(
            @Valid @RequestBody OrderCreateRequest request
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        OrderResponse response = orderService.createOrder(memberId, request);
        return ApiResponse.created(response);
    }

    @Operation(summary = "Get order", description = "Get order by ID (owner or admin only)")
    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<OrderResponse> getOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        OrderResponse response = orderService.getOrder(orderId, memberId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Get order by number", description = "Get order by order number (owner or admin only)")
    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<OrderResponse> getOrderByNumber(
            @Parameter(description = "Order number") @PathVariable String orderNumber
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        OrderResponse response = orderService.getOrderByNumber(orderNumber, memberId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Get my orders", description = "Get orders for authenticated member")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Page<OrderResponse>> getMyOrders(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        Page<OrderResponse> response = orderService.getMyOrders(memberId, pageable);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Search orders", description = "Search orders with conditions (admin)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<OrderResponse>> searchOrders(
            @Parameter(description = "Member ID") @RequestParam(required = false) Long memberId,
            @Parameter(description = "Order status") @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "Order number") @RequestParam(required = false) String orderNumber,
            @Parameter(description = "Start date") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam(required = false) LocalDateTime endDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        OrderSearchCondition condition = new OrderSearchCondition(
                memberId, status, orderNumber, startDate, endDate
        );
        Page<OrderResponse> response = orderService.searchOrders(condition, pageable);
        return ApiResponse.success(response);
    }

    // ========== Order Status Transitions ==========

    @Operation(summary = "Process payment", description = "Process payment for order")
    @PostMapping("/{orderId}/pay")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<OrderResponse> processPayment(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Valid @RequestBody PaymentRequest request
    ) {
        OrderResponse response = orderService.processPayment(
                orderId, request.paymentMethod(), request.transactionId()
        );
        return ApiResponse.success(response);
    }

    @Operation(summary = "Start preparing", description = "Start preparing order (seller)")
    @PostMapping("/{orderId}/prepare")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ApiResponse<OrderResponse> startPreparing(
            @Parameter(description = "Order ID") @PathVariable Long orderId
    ) {
        OrderResponse response = orderService.startPreparing(orderId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Ship order", description = "Ship order with tracking number")
    @PostMapping("/{orderId}/ship")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ApiResponse<OrderResponse> shipOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Parameter(description = "Tracking number") @RequestParam String trackingNumber
    ) {
        OrderResponse response = orderService.shipOrder(orderId, trackingNumber);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Deliver order", description = "Mark order as delivered")
    @PostMapping("/{orderId}/deliver")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ApiResponse<OrderResponse> deliverOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId
    ) {
        OrderResponse response = orderService.deliverOrder(orderId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Cancel order", description = "Cancel order")
    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<OrderResponse> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Parameter(description = "Cancel reason") @RequestParam String reason
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        OrderResponse response = orderService.cancelOrder(orderId, memberId, reason);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Cancel order item", description = "Cancel specific item in order")
    @PostMapping("/{orderId}/items/{itemId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<OrderResponse> cancelOrderItem(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Parameter(description = "Item ID") @PathVariable Long itemId,
            @Parameter(description = "Cancel reason") @RequestParam String reason
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        OrderResponse response = orderService.cancelOrderItem(orderId, memberId, itemId, reason);
        return ApiResponse.success(response);
    }
}

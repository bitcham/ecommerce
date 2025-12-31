package platform.ecommerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import platform.ecommerce.dto.request.cart.*;
import platform.ecommerce.dto.response.*;
import platform.ecommerce.dto.response.cart.*;
import platform.ecommerce.security.SecurityUtils;
import platform.ecommerce.service.cart.CartService;

/**
 * Cart REST controller.
 */
@Tag(name = "Cart", description = "Shopping cart API")
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get cart", description = "Get shopping cart for authenticated member")
    @GetMapping
    public ApiResponse<CartResponse> getCart() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        CartResponse response = cartService.getCartSummary(memberId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Add to cart", description = "Add item to cart")
    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CartItemResponse> addToCart(
            @Valid @RequestBody CartItemAddRequest request
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        CartItemResponse response = cartService.addToCart(memberId, request);
        return ApiResponse.created(response);
    }

    @Operation(summary = "Update quantity", description = "Update item quantity in cart")
    @PatchMapping("/items/{itemId}")
    public ApiResponse<CartItemResponse> updateQuantity(
            @Parameter(description = "Cart item ID") @PathVariable Long itemId,
            @Valid @RequestBody CartItemUpdateRequest request
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        CartItemResponse response = cartService.updateQuantity(memberId, itemId, request.quantity());
        return ApiResponse.success(response);
    }

    @Operation(summary = "Remove from cart", description = "Remove item from cart")
    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFromCart(
            @Parameter(description = "Cart item ID") @PathVariable Long itemId
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        cartService.removeFromCart(memberId, itemId);
    }

    @Operation(summary = "Clear cart", description = "Remove all items from cart")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        cartService.clearCart(memberId);
    }
}

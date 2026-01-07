package platform.ecommerce.service.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import platform.ecommerce.dto.request.cart.CartItemAddRequest;
import platform.ecommerce.dto.response.cart.CartItemResponse;
import platform.ecommerce.dto.response.cart.CartResponse;
import platform.ecommerce.service.cart.CartService;

/**
 * Cart application service.
 * Currently delegates to CartService - can be enhanced for DTO transformation later.
 */
@Service
@RequiredArgsConstructor
public class CartApplicationService {

    private final CartService cartService;

    /**
     * Get or create cart for a member.
     */
    public CartResponse getOrCreateCart(Long memberId) {
        return cartService.getOrCreateCart(memberId);
    }

    /**
     * Add item to cart.
     */
    public CartItemResponse addToCart(Long memberId, CartItemAddRequest request) {
        return cartService.addToCart(memberId, request);
    }

    /**
     * Update item quantity.
     */
    public CartItemResponse updateQuantity(Long memberId, Long cartItemId, int quantity) {
        return cartService.updateQuantity(memberId, cartItemId, quantity);
    }

    /**
     * Remove item from cart.
     */
    public void removeFromCart(Long memberId, Long cartItemId) {
        cartService.removeFromCart(memberId, cartItemId);
    }

    /**
     * Clear all items from cart.
     */
    public void clearCart(Long memberId) {
        cartService.clearCart(memberId);
    }

    /**
     * Get cart summary with product details.
     */
    public CartResponse getCartSummary(Long memberId) {
        return cartService.getCartSummary(memberId);
    }
}

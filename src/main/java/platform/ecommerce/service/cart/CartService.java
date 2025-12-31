package platform.ecommerce.service.cart;

import platform.ecommerce.dto.request.cart.CartItemAddRequest;
import platform.ecommerce.dto.response.cart.CartItemResponse;
import platform.ecommerce.dto.response.cart.CartResponse;

/**
 * Service interface for Cart operations.
 */
public interface CartService {

    /**
     * Gets the cart for a member. Creates one if it doesn't exist.
     */
    CartResponse getOrCreateCart(Long memberId);

    /**
     * Adds an item to the cart. Merges quantity if item already exists.
     */
    CartItemResponse addToCart(Long memberId, CartItemAddRequest request);

    /**
     * Updates the quantity of an item in the cart.
     */
    CartItemResponse updateQuantity(Long memberId, Long cartItemId, int quantity);

    /**
     * Removes an item from the cart.
     */
    void removeFromCart(Long memberId, Long cartItemId);

    /**
     * Clears all items from the cart.
     */
    void clearCart(Long memberId);

    /**
     * Gets the cart with all product details.
     */
    CartResponse getCartSummary(Long memberId);
}

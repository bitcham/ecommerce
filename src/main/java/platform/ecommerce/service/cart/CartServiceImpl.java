package platform.ecommerce.service.cart;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import platform.ecommerce.domain.cart.Cart;
import platform.ecommerce.domain.cart.CartItem;
import platform.ecommerce.dto.request.cart.CartItemAddRequest;
import platform.ecommerce.dto.response.cart.CartItemResponse;
import platform.ecommerce.dto.response.cart.CartResponse;
import platform.ecommerce.dto.response.product.ProductDetailResponse;
import platform.ecommerce.dto.response.product.ProductOptionResponse;
import platform.ecommerce.exception.EntityNotFoundException;
import platform.ecommerce.exception.ErrorCode;
import platform.ecommerce.repository.cart.CartRepository;
import platform.ecommerce.service.application.ProductApplicationService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Cart service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductApplicationService productApplicationService;

    @Override
    @Transactional
    public CartResponse getOrCreateCart(Long memberId) {
        Cart cart = cartRepository.findByMemberIdWithItems(memberId)
                .orElseGet(() -> createCart(memberId));
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartItemResponse addToCart(Long memberId, CartItemAddRequest request) {
        log.info("Adding item to cart for member: {}", memberId);

        // Validate product exists and is available
        ProductDetailResponse product = productApplicationService.getProductDetail(request.productId());
        validateProductAvailable(product, request.productOptionId());

        Cart cart = getOrCreateCartEntity(memberId);
        CartItem item = cart.addItem(request.productId(), request.productOptionId(), request.quantity());

        log.info("Item added to cart: memberId={}, productId={}", memberId, request.productId());
        return toItemResponse(item, product);
    }

    @Override
    @Transactional
    public CartItemResponse updateQuantity(Long memberId, Long cartItemId, int quantity) {
        log.info("Updating cart item quantity: memberId={}, itemId={}, quantity={}",
                memberId, cartItemId, quantity);

        Cart cart = findCartByMemberId(memberId);
        cart.updateItemQuantity(cartItemId, quantity);

        CartItem item = cart.findItemById(cartItemId);
        ProductDetailResponse product = productApplicationService.getProductDetail(item.getProductId());

        return toItemResponse(item, product);
    }

    @Override
    @Transactional
    public void removeFromCart(Long memberId, Long cartItemId) {
        log.info("Removing item from cart: memberId={}, itemId={}", memberId, cartItemId);

        Cart cart = findCartByMemberId(memberId);
        cart.removeItem(cartItemId);

        log.info("Item removed from cart: memberId={}, itemId={}", memberId, cartItemId);
    }

    @Override
    @Transactional
    public void clearCart(Long memberId) {
        log.info("Clearing cart for member: {}", memberId);

        Cart cart = findCartByMemberId(memberId);
        cart.clear();

        log.info("Cart cleared for member: {}", memberId);
    }

    @Override
    public CartResponse getCartSummary(Long memberId) {
        Cart cart = cartRepository.findByMemberIdWithItems(memberId)
                .orElse(Cart.builder().memberId(memberId).build());
        return toResponseWithProductDetails(cart);
    }

    // ========== Private Helper Methods ==========

    private Cart createCart(Long memberId) {
        log.info("Creating new cart for member: {}", memberId);
        Cart cart = Cart.builder().memberId(memberId).build();
        return cartRepository.save(cart);
    }

    private Cart getOrCreateCartEntity(Long memberId) {
        return cartRepository.findByMemberIdWithItems(memberId)
                .orElseGet(() -> createCart(memberId));
    }

    private Cart findCartByMemberId(Long memberId) {
        return cartRepository.findByMemberIdWithItems(memberId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CART_NOT_FOUND));
    }

    private void validateProductAvailable(ProductDetailResponse product, Long optionId) {
        if (optionId != null) {
            product.options().stream()
                    .filter(opt -> opt.id().equals(optionId))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_OPTION_NOT_FOUND));
        }
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::toBasicItemResponse)
                .toList();

        return CartResponse.builder()
                .id(cart.getId())
                .memberId(cart.getMemberId())
                .items(items)
                .itemCount(cart.getItemCount())
                .uniqueItemCount(cart.getUniqueItemCount())
                .subtotal(BigDecimal.ZERO)
                .build();
    }

    private CartResponse toResponseWithProductDetails(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::toItemResponseWithProduct)
                .toList();

        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .memberId(cart.getMemberId())
                .items(items)
                .itemCount(cart.getItemCount())
                .uniqueItemCount(cart.getUniqueItemCount())
                .subtotal(subtotal)
                .build();
    }

    private CartItemResponse toBasicItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productOptionId(item.getProductOptionId())
                .quantity(item.getQuantity())
                .available(true)
                .addedAt(item.getAddedAt())
                .build();
    }

    private CartItemResponse toItemResponseWithProduct(CartItem item) {
        try {
            ProductDetailResponse product = productApplicationService.getProductDetail(item.getProductId());
            return toItemResponse(item, product);
        } catch (EntityNotFoundException e) {
            // Product no longer available
            return CartItemResponse.builder()
                    .id(item.getId())
                    .productId(item.getProductId())
                    .productOptionId(item.getProductOptionId())
                    .productName("Product unavailable")
                    .quantity(item.getQuantity())
                    .unitPrice(BigDecimal.ZERO)
                    .subtotal(BigDecimal.ZERO)
                    .available(false)
                    .addedAt(item.getAddedAt())
                    .build();
        }
    }

    private CartItemResponse toItemResponse(CartItem item, ProductDetailResponse product) {
        String optionName = null;
        BigDecimal unitPrice = product.basePrice();

        if (item.getProductOptionId() != null) {
            ProductOptionResponse option = product.options().stream()
                    .filter(opt -> opt.id().equals(item.getProductOptionId()))
                    .findFirst()
                    .orElse(null);

            if (option != null) {
                optionName = option.optionValue();
                unitPrice = product.basePrice().add(option.additionalPrice());
            }
        }

        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productOptionId(item.getProductOptionId())
                .productName(product.name())
                .optionName(optionName)
                .unitPrice(unitPrice)
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .available(true)
                .addedAt(item.getAddedAt())
                .build();
    }
}

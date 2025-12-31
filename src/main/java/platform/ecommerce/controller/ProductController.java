package platform.ecommerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import platform.ecommerce.dto.request.product.*;
import platform.ecommerce.dto.response.*;
import platform.ecommerce.dto.response.product.*;
import platform.ecommerce.service.product.ProductService;

import java.math.BigDecimal;

/**
 * Product REST controller.
 */
@Tag(name = "Product", description = "Product management API")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Create product", description = "Create a new product (seller only)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ApiResponse<ProductResponse> createProduct(
            @Parameter(description = "Seller ID") @RequestParam Long sellerId,
            @Valid @RequestBody ProductCreateRequest request
    ) {
        ProductResponse response = productService.createProduct(sellerId, request);
        return ApiResponse.created(response);
    }

    @Operation(summary = "Get product", description = "Get product summary by ID")
    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> getProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId
    ) {
        ProductResponse response = productService.getProduct(productId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Get product detail", description = "Get product detail with options and images")
    @GetMapping("/{productId}/detail")
    public ApiResponse<ProductDetailResponse> getProductDetail(
            @Parameter(description = "Product ID") @PathVariable Long productId
    ) {
        ProductDetailResponse response = productService.getProductDetail(productId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Search products", description = "Search products with conditions")
    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> searchProducts(
            @Parameter(description = "Category ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Seller ID") @RequestParam(required = false) Long sellerId,
            @Parameter(description = "Keyword (searches name and description)") @RequestParam(required = false) String keyword,
            @Parameter(description = "Min price") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Max price") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Sort type: LATEST, PRICE_LOW, PRICE_HIGH, NAME_ASC") @RequestParam(required = false) ProductSortType sort,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        ProductSearchCondition condition = ProductSearchCondition.builder()
                .categoryId(categoryId)
                .sellerId(sellerId)
                .keyword(keyword)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .sortType(sort)
                .build();

        PageResponse<ProductResponse> response = productService.searchProducts(condition, pageable);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Update product", description = "Update product information")
    @PatchMapping("/{productId}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ApiResponse<ProductResponse> updateProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        ProductResponse response = productService.updateProduct(productId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Publish product", description = "Publish draft product to make it active")
    @PostMapping("/{productId}/publish")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ApiResponse<ProductResponse> publishProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId
    ) {
        ProductResponse response = productService.publishProduct(productId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Discontinue product", description = "Mark product as discontinued")
    @PostMapping("/{productId}/discontinue")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public void discontinueProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId
    ) {
        productService.discontinueProduct(productId);
    }

    @Operation(summary = "Delete product", description = "Soft delete product")
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public void deleteProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId
    ) {
        productService.deleteProduct(productId);
    }

    // ========== Option Endpoints ==========

    @Operation(summary = "Add option", description = "Add product option")
    @PostMapping("/{productId}/options")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ApiResponse<ProductOptionResponse> addOption(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Valid @RequestBody ProductOptionRequest request
    ) {
        ProductOptionResponse response = productService.addOption(productId, request);
        return ApiResponse.created(response);
    }

    @Operation(summary = "Remove option", description = "Remove product option")
    @DeleteMapping("/{productId}/options/{optionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public void removeOption(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Option ID") @PathVariable Long optionId
    ) {
        productService.removeOption(productId, optionId);
    }

    @Operation(summary = "Update option stock", description = "Update product option stock")
    @PatchMapping("/{productId}/options/{optionId}/stock")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ApiResponse<ProductOptionResponse> updateOptionStock(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Option ID") @PathVariable Long optionId,
            @Parameter(description = "New stock quantity") @RequestParam int stock
    ) {
        ProductOptionResponse response = productService.updateOptionStock(productId, optionId, stock);
        return ApiResponse.success(response);
    }

    // ========== Image Endpoints ==========

    @Operation(summary = "Add image", description = "Add product image")
    @PostMapping("/{productId}/images")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ApiResponse<ProductImageResponse> addImage(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Image URL") @RequestParam String imageUrl,
            @Parameter(description = "Alt text") @RequestParam(required = false) String altText
    ) {
        ProductImageResponse response = productService.addImage(productId, imageUrl, altText);
        return ApiResponse.created(response);
    }

    @Operation(summary = "Remove image", description = "Remove product image")
    @DeleteMapping("/{productId}/images/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public void removeImage(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Image ID") @PathVariable Long imageId
    ) {
        productService.removeImage(productId, imageId);
    }
}

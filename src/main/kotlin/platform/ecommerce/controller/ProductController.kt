package platform.ecommerce.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import platform.ecommerce.dto.request.ProductCreateRequest
import platform.ecommerce.dto.request.ProductOptionRequest
import platform.ecommerce.dto.response.ApiResponse
import platform.ecommerce.dto.response.ProductDetailResponse
import platform.ecommerce.dto.response.ProductListResponse
import platform.ecommerce.dto.response.ProductOptionResponse
import platform.ecommerce.mapper.ProductMapper
import platform.ecommerce.mapper.ProductOptionMapper
import platform.ecommerce.service.ProductService
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerResponse

@RestController
@RequestMapping("/products")
class ProductController(
    private val productService: ProductService,
    private val productMapper: ProductMapper,
    private val productOptionMapper: ProductOptionMapper
) {
    @Operation(summary = "Create a new product", description = "Creates a new product in the catalog")
    @ApiResponses(
        SwaggerResponse(responseCode = "201", description = "Product created successfully"),
        SwaggerResponse(responseCode = "400", description = "Invalid input")
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createProduct(@Valid @RequestBody request: ProductCreateRequest): ApiResponse<ProductDetailResponse> {
        val product = productService.createProduct(request)
        val response = productMapper.toDetailResponse(product)
        return ApiResponse.success(response, "Product created successfully")
    }

    @Operation(summary = "Delete a product", description = "Soft deletes an existing product from the catalog")
    @ApiResponses(
        SwaggerResponse(responseCode = "204", description = "Product deleted successfully")
    )
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProduct(@PathVariable productId: Long) {
        productService.deleteProduct(productId)
    }

    @Operation(summary = "Add a product option", description = "Adds an option to an existing product")
    @ApiResponses(
        SwaggerResponse(responseCode = "201", description = "Product option added successfully"),
        SwaggerResponse(responseCode = "400", description = "Invalid input")
    )
    @PostMapping("/{productId}/options")
    @ResponseStatus(HttpStatus.CREATED)
    fun addProductOption(
        @PathVariable productId: Long,
        @Valid @RequestBody request: ProductOptionRequest
    ): ApiResponse<ProductOptionResponse> {
        val option = productService.addProductOption(productId, request)
        val response = productOptionMapper.toResponse(option)
        return ApiResponse.success(response, "Product option added successfully")
    }

    @Operation(summary = "Remove a product option", description = "Removes an option from an existing product")
    @ApiResponses(
        SwaggerResponse(responseCode = "204", description = "Product option removed successfully")
    )
    @DeleteMapping("/{productId}/options/{optionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeProductOption(
        @PathVariable productId: Long,
        @PathVariable optionId: Long
    ) {
        productService.removeProductOption(productId, optionId)
    }

    @Operation(summary = "Get product details", description = "Retrieves detailed information of a product including options")
    @ApiResponses(
        SwaggerResponse(responseCode = "200", description = "Product retrieved successfully"),
        SwaggerResponse(responseCode = "404", description = "Product not found")
    )
    @GetMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    fun getProduct(@Valid @PathVariable productId: Long): ApiResponse<ProductDetailResponse> {
        val product = productService.getProduct(productId)
        val response = productMapper.toDetailResponse(product)
        return ApiResponse.success(response, "Product retrieved successfully")
    }

    @Operation(summary = "Get all products", description = "Retrieves list of all products with basic information")
    @ApiResponses(
        SwaggerResponse(responseCode = "200", description = "Products retrieved successfully")
    )
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getAllProducts(): ApiResponse<List<ProductListResponse>> {
        val products = productService.getAllProducts()
        val response = products.map { productMapper.toListResponse(it) }
        return ApiResponse.success(response, "Products retrieved successfully")
    }
}

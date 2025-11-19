package platform.ecommerce.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import platform.ecommerce.dto.request.ProductCreateRequest
import platform.ecommerce.dto.request.ProductOptionRequest
import platform.ecommerce.dto.response.ApiResponse
import platform.ecommerce.dto.response.ProductOptionResponse
import platform.ecommerce.dto.response.ProductResponse
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
    @ApiResponses(SwaggerResponse(responseCode = "201", description = "Product created successfully")
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createProduct(@RequestBody request: ProductCreateRequest): ApiResponse<ProductResponse> {
        val product = productService.createProduct(request)
        val response = productMapper.toResponse(product)
        return ApiResponse.success(response, "Product created successfully")
    }

    @Operation(summary = "Add a product option", description = "Adds an option to an existing product")
    @ApiResponses(
        SwaggerResponse(responseCode = "201", description = "Product option added successfully")
    )
    @PostMapping("/{productId}/options")
    @ResponseStatus(HttpStatus.CREATED)
    fun addProductOption(
        @PathVariable productId: Long,
        @RequestBody request: ProductOptionRequest
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
}
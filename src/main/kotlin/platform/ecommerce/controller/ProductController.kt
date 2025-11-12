package platform.ecommerce.controller

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

@RestController
@RequestMapping("/products")
class ProductController(
    private val productService: ProductService,
    private val productMapper: ProductMapper,
    private val productOptionMapper: ProductOptionMapper
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createProduct(@RequestBody request: ProductCreateRequest): ApiResponse<ProductResponse> {
        val product = productService.createProduct(request)
        val response = productMapper.toResponse(product)
        return ApiResponse.success(response, "Product created successfully")
    }

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
}
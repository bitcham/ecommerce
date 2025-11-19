package platform.ecommerce.fixture

import platform.ecommerce.domain.Product
import platform.ecommerce.domain.ProductOption
import platform.ecommerce.dto.request.ProductCreateRequest
import platform.ecommerce.dto.request.ProductOptionRequest
import platform.ecommerce.dto.response.ProductDetailResponse
import platform.ecommerce.dto.response.ProductListResponse
import platform.ecommerce.dto.response.ProductOptionResponse
import platform.ecommerce.enums.ProductStatus

object ProductFixture {

    fun createProductCreateRequest(
        sku: String = "PROD-001",
        name: String = "Sample Product",
        description: String = "This is a sample product.",
        price: Double = 99.9,
        imageUrl: String = "http://example.com/image.jpg"
    ): ProductCreateRequest {
        return ProductCreateRequest(
            sku = sku,
            name = name,
            description = description,
            price = price,
            imageUrl = imageUrl
        )
    }

    fun createProduct(
        id: Long? = null,
        sku: String = "PROD-001",
        name: String = "Sample Product",
        description: String = "This is a sample product.",
        price: Double = 99.9,
        imageUrl: String = "http://example.com/image.jpg"
    ): Product {
        return Product.create(
            sku = sku,
            name = name,
            description = description,
            price = price,
            imageUrl = imageUrl
        ).apply {
            id?.let {
                val idField = Product::class.java.getDeclaredField("id")
                idField.isAccessible = true
                idField.set(this, it)
            }
        }
    }

    fun createProductOptionRequest(
        optionName: String = "Red/M",
        stockQuantity: Int = 10
    ): ProductOptionRequest {
        return ProductOptionRequest(
            optionName = optionName,
            stockQuantity = stockQuantity
        )
    }

    fun createProductOptionResponse(
        id: Long = 1L,
        sku: String = "PROD-001-Red-M",
        optionName: String = "Red/M",
        stockQuantity: Int = 10
    ): ProductOptionResponse {
        return ProductOptionResponse(
            id = id,
            sku = sku,
            optionName = optionName,
            stockQuantity = stockQuantity
        )
    }

    fun createProductListResponse(
        id: Long = 1L,
        sku: String = "PROD-001",
        name: String = "Sample Product",
        price: Double = 99.9,
        imageUrl: String = "http://example.com/image.jpg",
        status: ProductStatus = ProductStatus.AVAILABLE
    ): ProductListResponse {
        return ProductListResponse(
            id = id,
            sku = sku,
            name = name,
            price = price,
            imageUrl = imageUrl,
            status = status
        )
    }

    fun createProductDetailResponse(
        id: Long = 1L,
        sku: String = "PROD-001",
        name: String = "Sample Product",
        description: String = "This is a sample product.",
        price: Double = 99.9,
        imageUrl: String = "http://example.com/image.jpg",
        status: ProductStatus = ProductStatus.AVAILABLE,
        options: List<ProductOptionResponse> = emptyList()
    ): ProductDetailResponse {
        return ProductDetailResponse(
            id = id,
            sku = sku,
            name = name,
            description = description,
            price = price,
            imageUrl = imageUrl,
            status = status,
            options = options
        )
    }

    fun setProductOptionId(option: ProductOption, id: Long): ProductOption {
        val idField = ProductOption::class.java.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(option, id)
        return option
    }
}

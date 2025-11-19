package platform.ecommerce.dto.response

import platform.ecommerce.enums.ProductStatus


data class ProductListResponse(
    val id: Long,
    val sku: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val status: ProductStatus
)
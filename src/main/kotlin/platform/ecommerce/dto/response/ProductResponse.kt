package platform.ecommerce.dto.response

import platform.ecommerce.enums.ProductStatus

data class ProductResponse(
    val id: Long,
    val sku: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val status: ProductStatus,
    val options: List<ProductOptionResponse>
)

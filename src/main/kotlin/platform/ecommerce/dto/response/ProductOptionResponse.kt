package platform.ecommerce.dto.response

data class ProductOptionResponse(
    val id: Long,
    val sku: String,
    val optionName: String,
    val stockQuantity: Int
)

package platform.ecommerce.dto.request

data class ProductCreateRequest(
    val sku: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String
)

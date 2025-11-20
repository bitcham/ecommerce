package platform.ecommerce.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class ProductCreateRequest(
    @field:NotBlank(message = "SKU is required")
    val sku: String,
    @field:NotBlank(message = "Name is required")
    val name: String,
    val description: String,
    @field:Positive(message = "Price must be positive")
    val price: Double,
    @field:NotBlank(message = "Image URL is required")
    val imageUrl: String
)

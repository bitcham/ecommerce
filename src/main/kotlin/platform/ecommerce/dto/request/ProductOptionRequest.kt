package platform.ecommerce.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero

data class ProductOptionRequest(
    @field:NotBlank(message = "Option name is required")
    val optionName: String,
    @field:PositiveOrZero(message = "Stock quantity must be zero or greater")
    val stockQuantity: Int
)

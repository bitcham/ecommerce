package platform.ecommerce.domain.vo

import jakarta.persistence.Embeddable
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * Address Value Object.
 * Represents billing/delivery addresses.
 */
@Embeddable
data class Address(
    @field:NotBlank(message = "Street address is required")
    @field:Size(max = 255, message = "Street address must not exceed 255 characters")
    val streetAddress: String? = null,

    @field:NotBlank(message = "City is required")
    @field:Size(max = 100, message = "City must not exceed 100 characters")
    val city: String? = null,

    @field:NotBlank(message = "Postal code is required")
    @field:Size(max = 20, message = "Postal code must not exceed 20 characters")
    val postalCode: String? = null
) {
}
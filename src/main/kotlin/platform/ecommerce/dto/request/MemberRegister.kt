package platform.ecommerce.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class MemberRegister(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    val password: String,

    @field:NotBlank(message = "First name is required")
    @field:Size(max = 50, message = "First name must not exceed 50 characters")
    val firstName: String,

    @field:NotBlank(message = "Last name is required")
    @field:Size(max = 50, message = "Last name must not exceed 50 characters")
    val lastName: String,

    @field:Size(max = 20, message = "Phone must not exceed 20 characters")
    val phone: String? = null
)

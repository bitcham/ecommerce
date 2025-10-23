package platform.ecommerce.exception

class DuplicateEmailException(
    email: String
) : RuntimeException("Email already exists: $email")

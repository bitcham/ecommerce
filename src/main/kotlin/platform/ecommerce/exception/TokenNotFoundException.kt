package platform.ecommerce.exception

class TokenNotFoundException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    constructor(message: String?) : this(message, null)
}
package platform.ecommerce.exception

class TokenExpiredException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    constructor(message: String?) : this(message, null)
}
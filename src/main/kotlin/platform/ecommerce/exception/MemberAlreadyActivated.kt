package platform.ecommerce.exception

class MemberAlreadyActivated (
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    constructor(message: String?) : this(message, null)
}
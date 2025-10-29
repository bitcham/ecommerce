package platform.ecommerce.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class Logger {
    companion object {
        val logger = KotlinLogging.logger {}
    }
}
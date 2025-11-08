package platform.ecommerce

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class EcommerceApplication

fun main(args: Array<String>) {
    runApplication<EcommerceApplication>(*args)
}

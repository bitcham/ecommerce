package platform.ecommerce.repository

import org.springframework.data.repository.Repository
import platform.ecommerce.domain.Product

interface ProductRepository: Repository<Product, Long> {
    fun save(product: Product): Product
    fun findById(productId: Long): Product?
}
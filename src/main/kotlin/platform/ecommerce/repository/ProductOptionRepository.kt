package platform.ecommerce.repository

import org.springframework.data.repository.Repository
import platform.ecommerce.domain.ProductOption

interface ProductOptionRepository : Repository<ProductOption, Long> {
    fun save(productOption: ProductOption): ProductOption
    fun findById(optionId: Long): ProductOption?
    fun findAllByProductId(productId: Long): List<ProductOption>
    fun existsByProductIdAndStockQuantityGreaterThan(productId: Long, stockQuantity: Int): Boolean
    fun deleteById(optionId: Long)
}

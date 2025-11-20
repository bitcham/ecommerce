package platform.ecommerce.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import platform.ecommerce.domain.Product

interface ProductRepository: Repository<Product, Long> {
    fun save(product: Product): Product

    fun findById(productId: Long): Product?

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.productOptions WHERE p.id = :productId")
    fun findWithOptionsById(productId: Long): Product?

    fun findAll(): List<Product>

    // Admin queries that include deleted products (bypasses @SQLRestriction)
    @Query(value = "SELECT * FROM product WHERE id = :productId", nativeQuery = true)
    fun findByIdIncludingDeleted(productId: Long): Product?

    @Query(value = "SELECT * FROM product", nativeQuery = true)
    fun findAllIncludingDeleted(): List<Product>
}

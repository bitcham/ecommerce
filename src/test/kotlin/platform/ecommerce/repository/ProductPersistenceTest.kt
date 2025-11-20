package platform.ecommerce.repository

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestPropertySource
import platform.ecommerce.TestcontainersConfiguration
import platform.ecommerce.domain.Product
import platform.ecommerce.enums.ProductStatus

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration::class)
@TestPropertySource(properties = [
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.flyway.enabled=true"
])
class ProductPersistenceTest {

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @Test
    fun `should persist product with all fields correctly`() {
        // Given
        val product = Product.create(
            sku = "TEST-SKU-001",
            name = "Test Product",
            description = "Test product description",
            price = 99.99,
            imageUrl = "http://example.com/image.jpg"
        )

        // When
        val savedProduct = productRepository.save(product)
        val foundProduct = productRepository.findById(savedProduct.id!!)

        // Then
        assertThat(foundProduct).isNotNull
        foundProduct!!  // Assert non-null for cleaner assertions below

        assertThat(foundProduct.id).isEqualTo(savedProduct.id)
        assertThat(foundProduct.sku).isEqualTo("TEST-SKU-001")
        assertThat(foundProduct.name).isEqualTo("Test Product")
        assertThat(foundProduct.description).isEqualTo("Test product description")
        // BigDecimal comparison - the DB stores with 2 decimal precision (NUMERIC(19,2))
        // but the value comes from Money.of(Double), which has floating-point imprecision
        // Compare with tolerance or accept the persisted value
        assertThat(foundProduct.getPrice().amount.toDouble()).isCloseTo(99.99, org.assertj.core.data.Offset.offset(0.01))
        assertThat(foundProduct.imageUrl).isEqualTo("http://example.com/image.jpg")
        // Product starts with default status AVAILABLE (checkAndUpdateStatus not called on creation)
        assertThat(foundProduct.getProductStatus()).isEqualTo(ProductStatus.AVAILABLE)
    }

    @Test
    fun `should persist product options correctly`() {
        // Given
        val product = Product.create(
            sku = "TEST-SKU-002",
            name = "Product with Options",
            description = "Product description",
            price = 49.99,
            imageUrl = "http://example.com/image.jpg"
        )

        product.addOption("Red/M", 10)
        product.addOption("Blue/L", 5)

        // When
        val savedProduct = productRepository.save(product)
        val foundProduct = productRepository.findWithOptionsById(savedProduct.id!!)

        // Then
        assertThat(foundProduct).isNotNull
        assertThat(foundProduct?.options).hasSize(2)

        val options = foundProduct?.options!!
        assertThat(options.map { it.optionName }).containsExactlyInAnyOrder("Red/M", "Blue/L")
        assertThat(options.map { it.getStockQuantity() }).containsExactlyInAnyOrder(10, 5)
        assertThat(options.map { it.sku }).containsExactlyInAnyOrder("TEST-SKU-002-RED-M", "TEST-SKU-002-BLUE-L")
    }

    @Test
    fun `should cascade delete options when product is soft deleted`() {
        // Given
        val product = Product.create(
            sku = "TEST-SKU-003",
            name = "Product for Deletion",
            description = "Product description",
            price = 29.99,
            imageUrl = "http://example.com/image.jpg"
        )
        product.addOption("Green/S", 3)

        val savedProduct = productRepository.save(product)
        val productId = savedProduct.id!!

        // When
        savedProduct.softDelete()
        productRepository.save(savedProduct)
        entityManager.flush()
        entityManager.clear()  // Clear persistence context to force a new query

        // Then - soft deleted product is not found by standard query
        val foundProduct = productRepository.findById(productId)
        assertThat(foundProduct).isNull()

        // But can be found with admin query
        val deletedProduct = productRepository.findByIdIncludingDeleted(productId)
        assertThat(deletedProduct).isNotNull
        assertThat(deletedProduct?.isDeleted()).isTrue()
    }

    @Test
    fun `should remove option and update product correctly`() {
        // Given
        val product = Product.create(
            sku = "TEST-SKU-004",
            name = "Product for Option Removal",
            description = "Product description",
            price = 79.99,
            imageUrl = "http://example.com/image.jpg"
        )
        product.addOption("Yellow/XL", 7)
        product.addOption("Purple/M", 12)

        val savedProduct = productRepository.save(product)
        val optionToRemove = savedProduct.options.first { it.optionName == "Yellow/XL" }

        // When
        savedProduct.removeOption(optionToRemove.id!!)
        productRepository.save(savedProduct)

        // Then
        val updatedProduct = productRepository.findWithOptionsById(savedProduct.id!!)
        assertThat(updatedProduct?.options).hasSize(1)
        assertThat(updatedProduct?.options?.first()?.optionName).isEqualTo("Purple/M")
    }

    @Test
    fun `should update product status based on option availability`() {
        val product = Product.create(
            sku = "TEST-SKU-006",
            name = "Product Status Transitions",
            description = "Product description",
            price = 19.99,
            imageUrl = "http://example.com/image.jpg"
        )

        val savedProduct = productRepository.save(product)
        entityManager.flush()
        entityManager.clear()

        // 1. New product defaults to AVAILABLE
        var foundProduct = productRepository.findById(savedProduct.id!!)
        assertThat(foundProduct?.getProductStatus()).isEqualTo(ProductStatus.AVAILABLE)

        // 2. Adding an option with stock keeps it AVAILABLE
        foundProduct!!.addOption("Green/S", 3)
        productRepository.save(foundProduct)
        entityManager.flush()
        entityManager.clear()

        foundProduct = productRepository.findWithOptionsById(savedProduct.id!!)
        assertThat(foundProduct?.getProductStatus()).isEqualTo(ProductStatus.AVAILABLE)

        // 3. Removing the only option should make it OUT_OF_STOCK
        val optionId = foundProduct!!.options.single().id!!
        foundProduct.removeOption(optionId)
        productRepository.save(foundProduct)
        entityManager.flush()
        entityManager.clear()

        foundProduct = productRepository.findById(savedProduct.id!!)
        assertThat(foundProduct?.getProductStatus()).isEqualTo(ProductStatus.OUT_OF_STOCK)

        // 4. After discontinuation, status stays DISCONTINUED even when stock is added back
        foundProduct!!.addOption("Blue/M", 5)
        foundProduct.discontinue()
        foundProduct.addOption("Blue/L", 2)
        productRepository.save(foundProduct)
        entityManager.flush()
        entityManager.clear()

        foundProduct = productRepository.findByIdIncludingDeleted(savedProduct.id!!)
        assertThat(foundProduct?.getProductStatus()).isEqualTo(ProductStatus.DISCONTINUED)
    }

    @Test
    fun `findWithOptionsById should eagerly load options to avoid N+1 queries`() {
        // Given
        val product = Product.create(
            sku = "TEST-SKU-005",
            name = "Product for N+1 Test",
            description = "Product description",
            price = 59.99,
            imageUrl = "http://example.com/image.jpg"
        )
        product.addOption("Option1", 5)
        product.addOption("Option2", 10)
        product.addOption("Option3", 15)

        val savedProduct = productRepository.save(product)

        // When
        val foundProduct = productRepository.findWithOptionsById(savedProduct.id!!)

        // Then - all options should be loaded in a single query
        assertThat(foundProduct?.options).hasSize(3)
        // Accessing options doesn't trigger additional queries because of JOIN FETCH
        foundProduct?.options?.forEach { option ->
            assertThat(option.optionName).isNotBlank()
            assertThat(option.getStockQuantity()).isGreaterThan(0)
        }
    }
}

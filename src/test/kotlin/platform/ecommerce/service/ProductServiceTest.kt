package platform.ecommerce.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.repository.findByIdOrNull
import platform.ecommerce.domain.vo.Money
import platform.ecommerce.enums.ProductStatus
import platform.ecommerce.exception.ProductNotFoundException
import platform.ecommerce.fixture.ProductFixture
import platform.ecommerce.repository.ProductRepository

@ExtendWith(MockitoExtension::class)
class ProductServiceTest {
    @Mock
    private lateinit var productRepository: ProductRepository

    @InjectMocks
    private lateinit var productService: ProductService

    @Test
    fun `should create product successfully`() {
        // Given
        val request = ProductFixture.createProductCreateRequest()
        val expectedProduct = ProductFixture.createProduct(id = 1L)

        whenever(productRepository.save(any())).thenReturn(expectedProduct)

        // When
        val createdProduct = productService.createProduct(request)

        // Then
        assertThat(createdProduct.id).isEqualTo(1L)
        assertThat(createdProduct.sku).isEqualTo("PROD-001")
        assertThat(createdProduct.name).isEqualTo("Sample Product")
        assertThat(createdProduct.description).isEqualTo("This is a sample product.")
        assertThat(createdProduct.getPrice()).isEqualTo(Money.of(99.9))
        assertThat(createdProduct.imageUrl).isEqualTo("http://example.com/image.jpg")
        assertThat(createdProduct.getProductStatus()).isEqualTo(ProductStatus.AVAILABLE)

        verify(productRepository).save(any())
    }

    @Test
    fun `should add product option successfully`() {
        // Given
        val productId = 1L
        val product = ProductFixture.createProduct(id = productId)
        val optionRequest = ProductFixture.createProductOptionRequest()

        whenever(productRepository.findById(productId)).thenReturn(product)

        // When
        val addedOption = productService.addProductOption(productId, optionRequest)

        // Then
        assertThat(addedOption.optionName).isEqualTo("Red/M")
        assertThat(addedOption.getStockQuantity()).isEqualTo(10)
        assertThat(addedOption.sku).isEqualTo("PROD-001-RED-M")
        assertThat(product.options).hasSize(1)
        assertThat(product.getProductStatus()).isEqualTo(ProductStatus.AVAILABLE)
    }

    @Test
    fun `should throw exception when adding option to non-existent product`() {
        // Given
        val productId = 999L
        val optionRequest = ProductFixture.createProductOptionRequest()

        whenever(productRepository.findById(productId)).thenReturn(null)

        // When & Then
        assertThatThrownBy { productService.addProductOption(productId, optionRequest) }
            .isInstanceOf(ProductNotFoundException::class.java)
            .hasMessageContaining("Product not found")
    }

    @Test
    fun `should remove product option successfully`() {
        // Given
        val productId = 1L
        val product = ProductFixture.createProduct(id = productId)
        product.addOption("Red/M", 10)
        val option = ProductFixture.setProductOptionId(product.options.first(), 1L)

        whenever(productRepository.findById(productId)).thenReturn(product)

        // When
        productService.removeProductOption(productId, option.id!!)

        // Then
        assertThat(product.options).isEmpty()
        assertThat(product.getProductStatus()).isEqualTo(ProductStatus.OUT_OF_STOCK)
    }

    @Test
    fun `should throw exception when removing option from non-existent product`() {
        // Given
        val productId = 999L
        val optionId = 1L

        whenever(productRepository.findById(productId)).thenReturn(null)

        // When & Then
        assertThatThrownBy { productService.removeProductOption(productId, optionId) }
            .isInstanceOf(ProductNotFoundException::class.java)
            .hasMessageContaining("Product not found")
    }

}
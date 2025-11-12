package platform.ecommerce.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester
import platform.ecommerce.config.SecurityConfig
import platform.ecommerce.exception.ProductNotFoundException
import platform.ecommerce.fixture.ProductFixture
import platform.ecommerce.mapper.ProductMapper
import platform.ecommerce.mapper.ProductOptionMapper
import platform.ecommerce.security.JwtUtil
import platform.ecommerce.service.ProductService

@WebMvcTest(controllers = [ProductController::class])
@Import(SecurityConfig::class)
class ProductControllerTest {

    @Autowired
    private lateinit var mockMvcTester: MockMvcTester

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var productService: ProductService

    @MockitoBean
    private lateinit var productMapper: ProductMapper

    @MockitoBean
    private lateinit var productOptionMapper: ProductOptionMapper

    @MockitoBean
    private lateinit var jwtUtil: JwtUtil

    @MockitoBean
    private lateinit var userDetailsService: UserDetailsService

    @Test
    @WithMockUser(authorities = ["SELLER"])
    fun `should create product successfully and return 201 Created`() {
        // Given
        val request = ProductFixture.createProductCreateRequest()
        val product = ProductFixture.createProduct(id = 1L)
        val response = ProductFixture.createProductResponse(id = 1L)

        given(productService.createProduct(any())).willReturn(product)
        given(productMapper.toResponse(any())).willReturn(response)

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .hasStatus(HttpStatus.CREATED)
            .bodyJson()
            .hasPathSatisfying("$.success") { assertThat(it).isEqualTo(true) }
            .hasPathSatisfying("$.message") { assertThat(it).isEqualTo("Product created successfully") }
            .hasPathSatisfying("$.data.id") { assertThat(it).isEqualTo(1) }
            .hasPathSatisfying("$.data.sku") { assertThat(it).isEqualTo("PROD-001") }
            .hasPathSatisfying("$.data.name") { assertThat(it).isEqualTo("Sample Product") }
            .hasPathSatisfying("$.data.price") { assertThat(it).isEqualTo(99.9) }
            .hasPathSatisfying("$.data.status") { assertThat(it).isEqualTo("AVAILABLE") }
    }

    @Test
    @WithMockUser(authorities = ["SELLER"])
    fun `should add product option successfully and return 201 Created`() {
        // Given
        val productId = 1L
        val optionRequest = ProductFixture.createProductOptionRequest()
        val product = ProductFixture.createProduct(id = productId)
        product.addOption(optionRequest.optionName, optionRequest.stockQuantity)
        val addedOption = product.options.first()
        val optionResponse = ProductFixture.createProductOptionResponse()

        given(productService.addProductOption(any(), any())).willReturn(addedOption)
        given(productOptionMapper.toResponse(any())).willReturn(optionResponse)

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/products/$productId/options")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(optionRequest)))
            .hasStatus(HttpStatus.CREATED)
            .bodyJson()
            .hasPathSatisfying("$.success") { assertThat(it).isEqualTo(true) }
            .hasPathSatisfying("$.message") { assertThat(it).isEqualTo("Product option added successfully") }
            .hasPathSatisfying("$.data.id") { assertThat(it).isEqualTo(1) }
            .hasPathSatisfying("$.data.sku") { assertThat(it).isEqualTo("PROD-001-Red-M") }
            .hasPathSatisfying("$.data.optionName") { assertThat(it).isEqualTo("Red/M") }
            .hasPathSatisfying("$.data.stockQuantity") { assertThat(it).isEqualTo(10) }
    }

    @Test
    @WithMockUser(authorities = ["SELLER"])
    fun `should return 404 Not Found when adding option to non-existent product`() {
        // Given
        val productId = 999L
        val optionRequest = ProductFixture.createProductOptionRequest()

        given(productService.addProductOption(any(), any()))
            .willThrow(ProductNotFoundException("Product not found: productId=$productId"))

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/products/$productId/options")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(optionRequest)))
            .hasStatus(HttpStatus.NOT_FOUND)
            .bodyJson()
            .hasPathSatisfying("$.success") { assertThat(it).isEqualTo(false) }
            .extractingPath("$.message").isEqualTo("Product not found: productId=$productId")
    }

    @Test
    @WithMockUser(authorities = ["SELLER"])
    fun `should return 400 Bad Request when creating product with missing fields`() {
        // Given
        val invalidRequest = objectMapper.writeValueAsString(mapOf(
            "name" to "Sample Product"
            // missing sku, price, etc.
        ))

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidRequest))
            .hasStatus(HttpStatus.BAD_REQUEST)
    }

    @Test
    @WithMockUser(authorities = ["CUSTOMER"])
    fun `should return 403 Forbidden when customer tries to create product`() {
        // Given
        val request = ProductFixture.createProductCreateRequest()

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .hasStatus(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `should return 403 Forbidden when creating product without authentication`() {
        // Given
        val request = ProductFixture.createProductCreateRequest()

        // When & Then
        assertThat(mockMvcTester.post()
            .uri("/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .hasStatus(HttpStatus.FORBIDDEN)
    }
}

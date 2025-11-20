package platform.ecommerce.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
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
        val response = ProductFixture.createProductDetailResponse(id = 1L)

        given(productService.createProduct(any())).willReturn(product)
        given(productMapper.toDetailResponse(any())).willReturn(response)

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
    val invalidRequest = ProductFixture.createProductCreateRequest(
        sku = " ",
        name = "",
        price = -1.0,
        imageUrl = ""
    )

    // When & Then
    assertThat(mockMvcTester.post()
        .uri("/products")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidRequest)))
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .hasPathSatisfying("$.message") { assertThat(it).isEqualTo("Validation failed") }
        .hasPathSatisfying("$.errors") {
            assertThat(it).asList().anySatisfy { error -> assertThat(error as String).contains("sku") }
            assertThat(it).asList().anySatisfy { error -> assertThat(error as String).contains("name") }
            assertThat(it).asList().anySatisfy { error -> assertThat(error as String).contains("price") }
            assertThat(it).asList().anySatisfy { error -> assertThat(error as String).contains("imageUrl") }
        }

    verify(productService, never()).createProduct(any())
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

    @Test
@WithMockUser(authorities = ["SELLER"])
fun `should return 400 Bad Request when adding option with invalid fields`() {
    // Given
    val productId = 1L
    val invalidOptionRequest = ProductFixture.createProductOptionRequest(optionName = "", stockQuantity = -5)

    // When & Then
    assertThat(mockMvcTester.post()
        .uri("/products/$productId/options")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidOptionRequest)))
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .hasPathSatisfying("$.message") { assertThat(it).isEqualTo("Validation failed") }
        .hasPathSatisfying("$.errors") {
            assertThat(it).asList().anySatisfy { error -> assertThat(error as String).contains("optionName") }
            assertThat(it).asList().anySatisfy { error -> assertThat(error as String).contains("stockQuantity") }
        }

    verify(productService, never()).addProductOption(any(), any())
}

    @Test
    @WithMockUser(authorities = ["SELLER"])
    fun `should return 204 No Content when seller removes productOption`() {
        // Given
        val productId = 1L
        val optionId = 1L

        // When & Then
        assertThat(mockMvcTester.delete()
            .uri("/products/$productId/options/$optionId"))
            .hasStatus(HttpStatus.NO_CONTENT)

        verify(productService).removeProductOption(productId, optionId)
    }

    @Test
    @WithMockUser
    fun `should get product successfully and return 200 OK`() {
        // Given
        val productId = 1L
        val product = ProductFixture.createProduct(id = productId)
        val response = ProductFixture.createProductDetailResponse(id = productId)

        given(productService.getProduct(productId)).willReturn(product)
        given(productMapper.toDetailResponse(any())).willReturn(response)

        // When & Then
        assertThat(mockMvcTester.get()
            .uri("/products/$productId"))
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .hasPathSatisfying("$.success") { assertThat(it).isEqualTo(true) }
            .hasPathSatisfying("$.message") { assertThat(it).isEqualTo("Product retrieved successfully") }
            .hasPathSatisfying("$.data.id") { assertThat(it).isEqualTo(1) }
            .hasPathSatisfying("$.data.sku") { assertThat(it).isEqualTo("PROD-001") }
            .hasPathSatisfying("$.data.name") { assertThat(it).isEqualTo("Sample Product") }
    }

    @Test
    @WithMockUser
    fun `should return 404 Not Found when getting non-existent product`() {
        // Given
        val productId = 999L

        given(productService.getProduct(productId))
            .willThrow(ProductNotFoundException("Product not found: productId=$productId"))

        // When & Then
        assertThat(mockMvcTester.get()
            .uri("/products/$productId"))
            .hasStatus(HttpStatus.NOT_FOUND)
            .bodyJson()
            .hasPathSatisfying("$.success") { assertThat(it).isEqualTo(false) }
            .extractingPath("$.message").isEqualTo("Product not found: productId=$productId")
    }

    @Test
    @WithMockUser
    fun `should get all products successfully and return 200 OK`() {
        // Given
        val product1 = ProductFixture.createProduct(id = 1L, sku = "PROD-001")
        val product2 = ProductFixture.createProduct(id = 2L, sku = "PROD-002")
        val response1 = ProductFixture.createProductListResponse(id = 1L, sku = "PROD-001")
        val response2 = ProductFixture.createProductListResponse(id = 2L, sku = "PROD-002")

        given(productService.getAllProducts()).willReturn(listOf(product1, product2))
        given(productMapper.toListResponse(product1)).willReturn(response1)
        given(productMapper.toListResponse(product2)).willReturn(response2)

        // When & Then
        assertThat(mockMvcTester.get()
            .uri("/products"))
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .hasPathSatisfying("$.success") { assertThat(it).isEqualTo(true) }
            .hasPathSatisfying("$.message") { assertThat(it).isEqualTo("Products retrieved successfully") }
            .hasPathSatisfying("$.data") { assertThat(it).asList().hasSize(2) }
    }

    @Test
    @WithMockUser(authorities = ["SELLER"])
    fun `should return 204 No Content when deleting product successfully`() {
        // Given
        val productId = 1L

        // When & Then
        assertThat(mockMvcTester.delete()
            .uri("/products/$productId"))
            .hasStatus(HttpStatus.NO_CONTENT)

        verify(productService).deleteProduct(productId)
    }

}

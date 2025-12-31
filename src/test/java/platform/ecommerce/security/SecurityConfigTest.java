package platform.ecommerce.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import platform.ecommerce.dto.response.category.CategoryResponse;
import platform.ecommerce.dto.response.product.ProductResponse;
import platform.ecommerce.service.category.CategoryService;
import platform.ecommerce.service.product.ProductService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security configuration tests for role-based access control.
 * Uses @SpringBootTest for full context with real security configuration.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Security Configuration Tests")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private CategoryService categoryService;

    @Nested
    @DisplayName("Public Endpoints")
    class PublicEndpoints {

        @Test
        @DisplayName("GET /api/v1/products/{id} should be accessible without authentication")
        void getProductPublic() throws Exception {
            given(productService.getProduct(1L))
                    .willReturn(ProductResponse.builder().id(1L).name("Test").build());

            mockMvc.perform(get("/api/v1/products/1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/categories/roots should be accessible without authentication")
        void getCategoriesPublic() throws Exception {
            given(categoryService.getRootCategories())
                    .willReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/categories/roots"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Admin-Only Endpoints")
    class AdminOnlyEndpoints {

        @Test
        @WithMockUser(roles = "MEMBER")
        @DisplayName("POST /api/v1/categories should be forbidden for MEMBER role")
        void createCategoryForbiddenForMember() throws Exception {
            mockMvc.perform(post("/api/v1/categories")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Test\",\"slug\":\"test\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("POST /api/v1/categories should be allowed for ADMIN role")
        void createCategoryAllowedForAdmin() throws Exception {
            given(categoryService.createCategory(any()))
                    .willReturn(CategoryResponse.builder().id(1L).name("Test").build());

            mockMvc.perform(post("/api/v1/categories")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Test\",\"slug\":\"test\",\"displayOrder\":1}"))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("DELETE /api/v1/categories/{id} should be forbidden for SELLER role")
        void deleteCategoryForbiddenForSeller() throws Exception {
            mockMvc.perform(delete("/api/v1/categories/1")
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("DELETE /api/v1/categories/{id} should be allowed for ADMIN role")
        void deleteCategoryAllowedForAdmin() throws Exception {
            mockMvc.perform(delete("/api/v1/categories/1")
                            .with(csrf()))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("Seller Endpoints")
    class SellerEndpoints {

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("POST /api/v1/products should be allowed for SELLER role")
        void createProductAllowedForSeller() throws Exception {
            given(productService.createProduct(any(), any()))
                    .willReturn(ProductResponse.builder().id(1L).name("Test").build());

            mockMvc.perform(post("/api/v1/products")
                            .with(csrf())
                            .param("sellerId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Test\",\"description\":\"Desc\",\"basePrice\":1000,\"categoryId\":1}"))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = "MEMBER")
        @DisplayName("POST /api/v1/products should be forbidden for MEMBER role")
        void createProductForbiddenForMember() throws Exception {
            mockMvc.perform(post("/api/v1/products")
                            .with(csrf())
                            .param("sellerId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Test\",\"description\":\"Desc\",\"basePrice\":1000,\"categoryId\":1}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("POST /api/v1/products should be allowed for ADMIN role")
        void createProductAllowedForAdmin() throws Exception {
            given(productService.createProduct(any(), any()))
                    .willReturn(ProductResponse.builder().id(1L).name("Test").build());

            mockMvc.perform(post("/api/v1/products")
                            .with(csrf())
                            .param("sellerId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Test\",\"description\":\"Desc\",\"basePrice\":1000,\"categoryId\":1}"))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("PATCH /api/v1/products/{id} should be allowed for SELLER role")
        void updateProductAllowedForSeller() throws Exception {
            given(productService.updateProduct(any(), any()))
                    .willReturn(ProductResponse.builder().id(1L).name("Updated").build());

            mockMvc.perform(patch("/api/v1/products/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Updated\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "MEMBER")
        @DisplayName("PATCH /api/v1/products/{id} should be forbidden for MEMBER role")
        void updateProductForbiddenForMember() throws Exception {
            mockMvc.perform(patch("/api/v1/products/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Updated\"}"))
                    .andExpect(status().isForbidden());
        }
    }
}

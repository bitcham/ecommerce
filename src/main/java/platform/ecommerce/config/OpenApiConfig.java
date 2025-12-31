package platform.ecommerce.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) configuration.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080/api").description("Local Development"),
                        new Server().url("https://api.ecommerce.com").description("Production")
                ))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme()))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    private Info apiInfo() {
        return new Info()
                .title("Coupang Clone E-Commerce API")
                .version("1.0.0")
                .description("""
                        ## E-Commerce Platform REST API

                        Production-grade backend API for Coupang-style e-commerce platform.

                        ### Features
                        - Member management & Authentication (JWT)
                        - Product & Category management
                        - Cart & Order processing
                        - Payment integration
                        - Review & Wishlist
                        - Coupon & Notification

                        ### Authentication
                        All endpoints except auth endpoints require Bearer token authentication.
                        """)
                .contact(new Contact()
                        .name("E-Commerce Team")
                        .email("dev@ecommerce.com"))
                .license(new License()
                        .name("Private")
                        .url("https://ecommerce.com"));
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT Access Token");
    }
}

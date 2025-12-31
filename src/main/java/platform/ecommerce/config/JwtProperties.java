package platform.ecommerce.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * JWT configuration properties.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    @NotBlank(message = "JWT secret is required")
    private String secret;

    @NotBlank(message = "JWT issuer is required")
    private String issuer = "ecommerce-platform";

    @Positive
    private long accessTokenExpiration = 900_000; // 15 minutes

    @Positive
    private long refreshTokenExpiration = 604_800_000; // 7 days
}

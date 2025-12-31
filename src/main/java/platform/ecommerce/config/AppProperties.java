package platform.ecommerce.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Application-specific configuration properties.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    @NotBlank
    private String name = "Coupang Clone E-Commerce";

    private String version = "1.0.0";

    @NotBlank
    private String baseUrl = "http://localhost:8080";

    private Mail mail = new Mail();

    private EmailVerification emailVerification = new EmailVerification();

    @Getter
    @Setter
    public static class Mail {
        @NotBlank
        private String from = "noreply@ecommerce.com";
    }

    @Getter
    @Setter
    public static class EmailVerification {
        @Positive
        private int expirationHours = 24;
    }
}

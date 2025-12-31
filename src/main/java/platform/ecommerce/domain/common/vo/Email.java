package platform.ecommerce.domain.common.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * Value Object representing an email address.
 * Immutable with validation.
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Email {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    @Column(name = "email", length = 255)
    private String value;

    private Email(String value) {
        this.value = value.toLowerCase().trim();
    }

    public static Email of(String email) {
        validate(email);
        return new Email(email);
    }

    private static void validate(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }

    public String getDomain() {
        return value.substring(value.indexOf('@') + 1);
    }

    public String getLocalPart() {
        return value.substring(0, value.indexOf('@'));
    }

    public String masked() {
        String local = getLocalPart();
        String domain = getDomain();
        if (local.length() <= 2) {
            return local.charAt(0) + "***@" + domain;
        }
        return local.substring(0, 2) + "***@" + domain;
    }

    @Override
    public String toString() {
        return value;
    }
}

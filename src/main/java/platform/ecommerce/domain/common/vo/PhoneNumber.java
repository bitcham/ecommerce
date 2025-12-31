package platform.ecommerce.domain.common.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * Value Object representing a Korean phone number.
 * Immutable with validation.
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhoneNumber {

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^01[016789]-?\\d{3,4}-?\\d{4}$"
    );

    @Column(name = "phone", length = 20)
    private String value;

    private PhoneNumber(String value) {
        this.value = normalize(value);
    }

    public static PhoneNumber of(String phone) {
        validate(phone);
        return new PhoneNumber(phone);
    }

    private static void validate(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        String normalized = phone.replaceAll("-", "");
        if (!PHONE_PATTERN.matcher(phone).matches() && !normalized.matches("^01[016789]\\d{7,8}$")) {
            throw new IllegalArgumentException("Invalid phone number format: " + phone);
        }
    }

    private String normalize(String phone) {
        String digits = phone.replaceAll("-", "");
        if (digits.length() == 10) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
        }
        return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
    }

    public String masked() {
        String[] parts = value.split("-");
        if (parts.length != 3) {
            return "***-****-****";
        }
        return parts[0] + "-****-" + parts[2];
    }

    @Override
    public String toString() {
        return value;
    }
}

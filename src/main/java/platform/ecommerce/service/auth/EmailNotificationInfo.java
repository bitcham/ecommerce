package platform.ecommerce.service.auth;

/**
 * Internal result type for operations that trigger email notifications.
 * Contains all information needed to send verification or password reset emails.
 */
public record EmailNotificationInfo(
        String email,
        String name,
        String token
) {
    public static EmailNotificationInfo of(String email, String name, String token) {
        return new EmailNotificationInfo(email, name, token);
    }
}

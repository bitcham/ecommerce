package platform.ecommerce.service.email;

/**
 * Email service interface for sending various types of emails.
 */
public interface EmailService {

    /**
     * Send email verification link to new user.
     *
     * @param to recipient email address
     * @param name recipient name
     * @param verificationToken the verification token
     */
    void sendVerificationEmail(String to, String name, String verificationToken);

    /**
     * Send password reset link.
     *
     * @param to recipient email address
     * @param name recipient name
     * @param resetToken the password reset token
     */
    void sendPasswordResetEmail(String to, String name, String resetToken);

    /**
     * Send order confirmation email.
     *
     * @param to recipient email address
     * @param name recipient name
     * @param orderNumber the order number
     */
    void sendOrderConfirmationEmail(String to, String name, String orderNumber);

    /**
     * Send order shipped notification.
     *
     * @param to recipient email address
     * @param name recipient name
     * @param orderNumber the order number
     * @param trackingNumber the shipping tracking number
     */
    void sendOrderShippedEmail(String to, String name, String orderNumber, String trackingNumber);
}

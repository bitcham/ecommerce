package platform.ecommerce.service.email;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * EmailService unit tests.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Tests")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @Captor
    private ArgumentCaptor<MimeMessage> messageCaptor;

    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender);
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@test.com");
        ReflectionTestUtils.setField(emailService, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(emailService, "appName", "Test E-Commerce");
    }

    @Nested
    @DisplayName("Send Verification Email")
    class SendVerificationEmail {

        @Test
        @DisplayName("Should send verification email successfully")
        void sendVerificationEmail_shouldSucceed() {
            // given
            String to = "user@example.com";
            String name = "홍길동";
            String token = "verification-token-123";
            given(mailSender.createMimeMessage()).willReturn(mimeMessage);

            // when
            emailService.sendVerificationEmail(to, name, token);

            // then
            verify(mailSender).createMimeMessage();
            verify(mailSender).send(messageCaptor.capture());
            assertThat(messageCaptor.getValue()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Send Password Reset Email")
    class SendPasswordResetEmail {

        @Test
        @DisplayName("Should send password reset email successfully")
        void sendPasswordResetEmail_shouldSucceed() {
            // given
            String to = "user@example.com";
            String name = "홍길동";
            String token = "reset-token-123";
            given(mailSender.createMimeMessage()).willReturn(mimeMessage);

            // when
            emailService.sendPasswordResetEmail(to, name, token);

            // then
            verify(mailSender).createMimeMessage();
            verify(mailSender).send(any(MimeMessage.class));
        }
    }

    @Nested
    @DisplayName("Send Order Confirmation Email")
    class SendOrderConfirmationEmail {

        @Test
        @DisplayName("Should send order confirmation email successfully")
        void sendOrderConfirmationEmail_shouldSucceed() {
            // given
            String to = "user@example.com";
            String name = "홍길동";
            String orderNumber = "ORD-2024-001";
            given(mailSender.createMimeMessage()).willReturn(mimeMessage);

            // when
            emailService.sendOrderConfirmationEmail(to, name, orderNumber);

            // then
            verify(mailSender).createMimeMessage();
            verify(mailSender).send(any(MimeMessage.class));
        }
    }

    @Nested
    @DisplayName("Send Order Shipped Email")
    class SendOrderShippedEmail {

        @Test
        @DisplayName("Should send order shipped email successfully")
        void sendOrderShippedEmail_shouldSucceed() {
            // given
            String to = "user@example.com";
            String name = "홍길동";
            String orderNumber = "ORD-2024-001";
            String trackingNumber = "1234567890";
            given(mailSender.createMimeMessage()).willReturn(mimeMessage);

            // when
            emailService.sendOrderShippedEmail(to, name, orderNumber, trackingNumber);

            // then
            verify(mailSender).createMimeMessage();
            verify(mailSender).send(any(MimeMessage.class));
        }
    }
}

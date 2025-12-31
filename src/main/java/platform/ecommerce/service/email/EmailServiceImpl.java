package platform.ecommerce.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Email service implementation using JavaMailSender.
 * All email sending is done asynchronously.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.name}")
    private String appName;

    @Override
    @Async
    public void sendVerificationEmail(String to, String name, String verificationToken) {
        log.info("Sending verification email to: {}", to);

        String subject = String.format("[%s] 이메일 인증을 완료해주세요", appName);
        String verificationLink = String.format("%s/api/v1/auth/verify-email?token=%s", baseUrl, verificationToken);

        String content = buildVerificationEmailContent(name, verificationLink);

        sendHtmlEmail(to, subject, content);
        log.info("Verification email sent successfully to: {}", to);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String name, String resetToken) {
        log.info("Sending password reset email to: {}", to);

        String subject = String.format("[%s] 비밀번호 재설정 안내", appName);
        String resetLink = String.format("%s/reset-password?token=%s", baseUrl, resetToken);

        String content = buildPasswordResetEmailContent(name, resetLink);

        sendHtmlEmail(to, subject, content);
        log.info("Password reset email sent successfully to: {}", to);
    }

    @Override
    @Async
    public void sendOrderConfirmationEmail(String to, String name, String orderNumber) {
        log.info("Sending order confirmation email to: {} for order: {}", to, orderNumber);

        String subject = String.format("[%s] 주문이 완료되었습니다 (#%s)", appName, orderNumber);

        String content = buildOrderConfirmationEmailContent(name, orderNumber);

        sendHtmlEmail(to, subject, content);
        log.info("Order confirmation email sent successfully to: {}", to);
    }

    @Override
    @Async
    public void sendOrderShippedEmail(String to, String name, String orderNumber, String trackingNumber) {
        log.info("Sending order shipped email to: {} for order: {}", to, orderNumber);

        String subject = String.format("[%s] 주문이 발송되었습니다 (#%s)", appName, orderNumber);

        String content = buildOrderShippedEmailContent(name, orderNumber, trackingNumber);

        sendHtmlEmail(to, subject, content);
        log.info("Order shipped email sent successfully to: {}", to);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to create email message to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to create email message", e);
        } catch (MailException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildVerificationEmailContent(String name, String verificationLink) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #4F46E5; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; background: #4F46E5; color: white; padding: 14px 28px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                    .footer { text-align: center; color: #6b7280; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                    </div>
                    <div class="content">
                        <h2>안녕하세요, %s님!</h2>
                        <p>회원가입을 환영합니다. 아래 버튼을 클릭하여 이메일 인증을 완료해주세요.</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">이메일 인증하기</a>
                        </p>
                        <p>버튼이 작동하지 않는 경우, 아래 링크를 브라우저에 복사하여 붙여넣으세요:</p>
                        <p style="word-break: break-all; color: #6b7280; font-size: 14px;">%s</p>
                        <p><strong>이 링크는 24시간 동안만 유효합니다.</strong></p>
                    </div>
                    <div class="footer">
                        <p>본 메일은 발신 전용입니다. 문의사항은 고객센터를 이용해주세요.</p>
                        <p>&copy; %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, appName, name, verificationLink, verificationLink, appName);
    }

    private String buildPasswordResetEmailContent(String name, String resetLink) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #DC2626; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; background: #DC2626; color: white; padding: 14px 28px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                    .footer { text-align: center; color: #6b7280; font-size: 12px; margin-top: 20px; }
                    .warning { background: #FEF3C7; border: 1px solid #F59E0B; padding: 12px; border-radius: 6px; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>비밀번호 재설정</h1>
                    </div>
                    <div class="content">
                        <h2>안녕하세요, %s님</h2>
                        <p>비밀번호 재설정 요청이 접수되었습니다. 아래 버튼을 클릭하여 새 비밀번호를 설정해주세요.</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">비밀번호 재설정</a>
                        </p>
                        <div class="warning">
                            <strong>⚠️ 주의:</strong> 본인이 요청하지 않았다면 이 이메일을 무시하세요. 비밀번호는 변경되지 않습니다.
                        </div>
                        <p><strong>이 링크는 1시간 동안만 유효합니다.</strong></p>
                    </div>
                    <div class="footer">
                        <p>&copy; %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, name, resetLink, appName);
    }

    private String buildOrderConfirmationEmailContent(String name, String orderNumber) {
        String orderDetailLink = String.format("%s/orders/%s", baseUrl, orderNumber);

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #059669; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .order-box { background: white; border: 1px solid #e5e7eb; padding: 20px; border-radius: 6px; margin: 20px 0; }
                    .button { display: inline-block; background: #059669; color: white; padding: 14px 28px; text-decoration: none; border-radius: 6px; }
                    .footer { text-align: center; color: #6b7280; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✓ 주문 완료</h1>
                    </div>
                    <div class="content">
                        <h2>%s님, 주문이 완료되었습니다!</h2>
                        <div class="order-box">
                            <p><strong>주문번호:</strong> %s</p>
                            <p>결제가 확인되면 상품 준비를 시작합니다.</p>
                        </div>
                        <p style="text-align: center;">
                            <a href="%s" class="button">주문 상세보기</a>
                        </p>
                    </div>
                    <div class="footer">
                        <p>&copy; %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, name, orderNumber, orderDetailLink, appName);
    }

    private String buildOrderShippedEmailContent(String name, String orderNumber, String trackingNumber) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #2563EB; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .tracking-box { background: white; border: 1px solid #e5e7eb; padding: 20px; border-radius: 6px; margin: 20px 0; text-align: center; }
                    .tracking-number { font-size: 24px; font-weight: bold; color: #2563EB; }
                    .footer { text-align: center; color: #6b7280; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📦 배송 시작</h1>
                    </div>
                    <div class="content">
                        <h2>%s님, 주문하신 상품이 발송되었습니다!</h2>
                        <p><strong>주문번호:</strong> %s</p>
                        <div class="tracking-box">
                            <p>운송장 번호</p>
                            <p class="tracking-number">%s</p>
                        </div>
                        <p>배송 현황은 택배사 홈페이지에서 확인하실 수 있습니다.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, name, orderNumber, trackingNumber, appName);
    }
}

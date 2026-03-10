package sub.boke.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import sub.boke.domain.VerificationCode;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationCode(String toEmail, String code, VerificationCode.CodeType type) {
        try {
            System.out.println("📧 开始发送邮件到: " + toEmail);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);

            if (type == VerificationCode.CodeType.REGISTER) {
                message.setSubject("注册验证码 - 个人博客");
                message.setText("您的注册验证码是：" + code + "\n验证码有效期为10分钟。");
            } else {
                message.setSubject("密码重置验证码 - 个人博客");
                message.setText("您的密码重置验证码是：" + code + "\n验证码有效期为10分钟。");
            }

            mailSender.send(message);
            System.out.println("✅ 邮件发送成功到: " + toEmail);
        } catch (Exception e) {
            System.out.println("❌ 邮件发送失败: " + e.getMessage());
            throw new RuntimeException("邮件发送失败", e);
        }
    }
}
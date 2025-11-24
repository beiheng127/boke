package sub.boke.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 添加这个导入
import sub.boke.domain.VerificationCode;
import sub.boke.repository.VerificationCodeRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;

    public String generateCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }

    // 添加 @Transactional 注解
    @Transactional
    public void sendVerificationCode(String email, VerificationCode.CodeType type) {
        // 使之前的验证码失效
        verificationCodeRepository.invalidatePreviousCodes(email, type);

        // 生成新验证码
        String code = generateCode();
        Instant expiresAt = Instant.now().plusSeconds(600); // 10分钟过期

        VerificationCode verificationCode = VerificationCode.builder()
                .email(email)
                .code(code)
                .type(type)
                .expiresAt(expiresAt)
                .isUsed(false)
                .build();

        verificationCodeRepository.save(verificationCode);

        // 发送邮件
        emailService.sendVerificationCode(email, code, type);
    }

    // 添加 @Transactional 注解
    @Transactional
    public boolean verifyCode(String email, String code, VerificationCode.CodeType type) {
        System.out.println("🔍 验证验证码 - 邮箱: " + email + ", 验证码: " + code + ", 类型: " + type);

        Optional<VerificationCode> verificationCodeOpt = verificationCodeRepository
                .findTopByEmailAndTypeAndIsUsedFalseOrderByCreatedAtDesc(email, type);

        if (verificationCodeOpt.isEmpty()) {
            System.out.println("❌ 未找到对应的验证码记录");
            return false;
        }

        VerificationCode verificationCode = verificationCodeOpt.get();
        System.out.println("🔍 找到验证码记录: " + verificationCode.getCode() + ", 过期时间: " + verificationCode.getExpiresAt());

        if (verificationCode.isExpired()) {
            System.out.println("❌ 验证码已过期");
            return false;
        }

        if (!verificationCode.getCode().equals(code)) {
            System.out.println("❌ 验证码不匹配 - 输入: " + code + ", 存储: " + verificationCode.getCode());
            return false;
        }

        // 标记为已使用
        verificationCode.setIsUsed(true);
        verificationCodeRepository.save(verificationCode);

        System.out.println("✅ 验证码验证成功");
        return true;
    }
}
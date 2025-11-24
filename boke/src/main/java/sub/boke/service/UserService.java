package sub.boke.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sub.boke.domain.User;
import sub.boke.repository.UserRepository;

import java.time.Instant;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 添加缺失的方法
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // 新增：根据邮箱查找用户
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // 修改注册方法，添加邮箱参数
    public User register(String username, String email, String password, User.Role role, String displayName) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建新用户，设置默认头像
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(role)
                .displayName(displayName != null ? displayName : username)
                .avatarUrl("/uploads/images/default/touxiang.jpg") // 设置默认头像
                .signature("") // 添加个性签名字段初始化
                .createdAt(Instant.now())
                .build();

        return userRepository.save(user);
    }

    public User resetPassword(String email, String newPassword) {
        System.out.println("🔍 开始重置密码 - 邮箱: " + email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("❌ 用户不存在: " + email);
                    return new RuntimeException("用户不存在");
                });

        System.out.println("🔍 找到用户: " + user.getUsername());

        if (newPassword == null || newPassword.trim().isEmpty()) {
            System.out.println("❌ 新密码为空");
            throw new RuntimeException("新密码不能为空");
        }

        if (newPassword.length() < 6) {
            System.out.println("❌ 新密码长度不足: " + newPassword.length());
            throw new RuntimeException("密码长度至少6位");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        System.out.println("✅ 密码重置完成");
        return user;
    }

    public Optional<User> validateUser(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> {
                    boolean matches = passwordEncoder.matches(password, user.getPasswordHash());
                    if (!matches) {
                        System.out.println("密码不匹配: 输入=" + password + ", 存储=" + user.getPasswordHash());
                    }
                    return matches;
                });
    }

    // 在 UserService 类中添加这些方法
    public Page<User> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User updateUserRole(Long userId, User.Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setRole(role);
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("用户不存在");
        }
        userRepository.deleteById(userId);
    }

    // 更新用户信息（包括个性签名）
    public User updateUser(Long userId, String displayName, String signature) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (displayName != null && !displayName.trim().isEmpty()) {
            user.setDisplayName(displayName.trim());
        }

        if (signature != null) {
            user.setSignature(signature.trim());
        }

        return userRepository.save(user);
    }

    // 更新用户头像
    public User updateAvatar(Long userId, String avatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setAvatarUrl(avatarUrl);
        return userRepository.save(user);
    }

    // 根据ID获取用户
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
}
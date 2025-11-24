package sub.boke.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sub.boke.domain.User;
import sub.boke.domain.VerificationCode;
import sub.boke.repository.UserRepository;
import sub.boke.security.TokenService;
import sub.boke.service.UserService;
import sub.boke.service.VerificationCodeService;
import sub.boke.util.RedisUtil;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {
    private final UserService userService;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final VerificationCodeService verificationCodeService;

    @Autowired
    private RedisUtil redisUtil;
    // Redis 缓存键前缀
    private static final String USER_SESSION_PREFIX = "user_session:";
    private static final String USER_INFO_PREFIX = "user_info:";
    private static final long SESSION_EXPIRE_HOURS = 24; // 会话缓存24小时


    public AuthController(UserService userService, TokenService tokenService,
                          UserRepository userRepository, VerificationCodeService verificationCodeService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.verificationCodeService = verificationCodeService;
    }

    // 修改注册请求，添加邮箱和验证码
    public record RegisterRequest(@NotBlank String username, @NotBlank @Email String email,
                                  @NotBlank String password, String displayName,
                                  @NotBlank String verificationCode) {
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }


    public record SendCodeRequest(@NotBlank @Email String email, @NotBlank String type) {
    }

    public record VerifyCodeRequest(@NotBlank @Email String email,
                                    @NotBlank String code,
                                    @NotBlank String type) {
    }

    public record ResetPasswordRequest(@NotBlank @Email String email,
                                       @NotBlank String code,
                                       @NotBlank String newPassword) {
    }

    // 发送验证码
    @PostMapping("/send-code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody SendCodeRequest req) {
        try {
            System.out.println("🔍 收到发送验证码请求 - 邮箱: " + req.email() + ", 类型: " + req.type());

            VerificationCode.CodeType type;
            try {
                type = VerificationCode.CodeType.valueOf(req.type().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("❌ 无效的验证码类型: " + req.type());
                return ResponseEntity.badRequest().body(Map.of("message", "无效的验证码类型"));
            }

            // 验证邮箱状态
            if (type == VerificationCode.CodeType.REGISTER) {
                System.out.println("🔍 检查邮箱是否已存在: " + req.email());
                if (userService.findByEmail(req.email()).isPresent()) {
                    System.out.println("❌ 邮箱已被注册: " + req.email());
                    return ResponseEntity.badRequest().body(Map.of("message", "邮箱已被注册"));
                }
            }
            // 如果是重置密码，检查邮箱是否存在
            else if (type == VerificationCode.CodeType.RESET_PASSWORD) {
                System.out.println("🔍 检查邮箱是否已注册: " + req.email());
                if (userService.findByEmail(req.email()).isEmpty()) {
                    System.out.println("❌ 邮箱未注册: " + req.email());
                    return ResponseEntity.badRequest().body(Map.of("message", "邮箱未注册"));
                }
            }

            System.out.println("✅ 开始发送验证码到: " + req.email());
            verificationCodeService.sendVerificationCode(req.email(), type);
            System.out.println("✅ 验证码发送成功");
            return ResponseEntity.ok(Map.of("message", "验证码已发送"));
        } catch (Exception e) {
            System.out.println("❌ 发送验证码失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", "发送验证码失败: " + e.getMessage()));
        }
    }

    // 验证验证码
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyCodeRequest req) {
        try {
            VerificationCode.CodeType type;
            try {
                type = VerificationCode.CodeType.valueOf(req.type().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("message", "无效的验证码类型"));
            }

            boolean isValid = verificationCodeService.verifyCode(req.email(), req.code(), type);
            if (isValid) {
                return ResponseEntity.ok(Map.of("message", "验证码正确"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "验证码错误或已过期"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "验证失败: " + e.getMessage()));
        }
    }
    // 修改注册方法
    //User.Role role = "BLOGGER".equalsIgnoreCase(req.role()) ? User.Role.BLOGGER : User.Role.VIEWER;
    // 修改注册方法
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            // 验证验证码
            boolean isCodeValid = verificationCodeService.verifyCode(
                    req.email(), req.verificationCode(), VerificationCode.CodeType.REGISTER);

            if (!isCodeValid) {
                return ResponseEntity.badRequest().body(Map.of("message", "验证码错误或已过期"));
            }

            User.Role role = User.Role.VIEWER;
            if (userService.findByUsername(req.username()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户名已存在"));
            }

            User user = userService.register(req.username(), req.email(), req.password(), role, req.displayName());
            String token = tokenService.issueToken(user.getId());

            //缓存用户信息
            cacheUserInfo(user);

            return buildAuthResponse(token, user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 在 AuthController 的 login 方法中，确保使用正确的方法
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        // 使用 validateUser 方法而不是 findByUsername + 手动密码验证
        return userService.validateUser(req.username(), req.password())
                .<ResponseEntity<?>>map(user -> {
                    System.out.println("🔍 登录成功 - 用户: " + user.getUsername() + ", 角色: " + user.getRole());
                    String token = tokenService.issueToken(user.getId());

                    cacheUserInfo(user);

                    return buildAuthResponse(token, user);
                })
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("message", "用户名或密码错误")));
    }

    // 重置密码
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        try {
            System.out.println("🔍 重置密码请求 - 邮箱: " + req.email() + ", 验证码: " + req.code() + ", 新密码长度: " + req.newPassword().length());

            // 验证验证码
            System.out.println("🔍 开始验证验证码...");
            boolean isCodeValid = verificationCodeService.verifyCode(
                    req.email(), req.code(), VerificationCode.CodeType.RESET_PASSWORD);

            if (!isCodeValid) {
                System.out.println("❌ 验证码验证失败");
                return ResponseEntity.badRequest().body(Map.of("message", "验证码错误或已过期"));
            }

            System.out.println("✅ 验证码验证成功");

            System.out.println("🔍 开始重置密码...");
            User user = userService.resetPassword(req.email(), req.newPassword());
            System.out.println("✅ 密码重置成功");

            // 清除用户缓存
            clearUserCache(user.getId());

            return ResponseEntity.ok(Map.of("message", "密码重置成功"));
        } catch (RuntimeException e) {
            System.out.println("❌ 重置密码失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            Long userId = tokenService.verifyToken(token);
            if (userId != null) {
                clearUserCache(userId);
                System.out.println("✅ 用户缓存已清除: " + userId);
            }

            tokenService.revoke(token);
        }
        return ResponseEntity.ok(Map.of("message", "已注销"));
    }


    // 验证token有效性
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("valid", false, "message", "未提供有效的认证令牌"));
        }

        String token = authHeader.substring(7);
        Long userId = tokenService.verifyToken(token);

        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("valid", false, "message", "令牌无效或已过期"));
        }

        // 从缓存获取用户信息
        User user = getUserFromCache(userId);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("valid", false, "message", "用户信息不存在"));
        }

        return ResponseEntity.ok(Map.of(
                "valid", true,
                "user", buildUserResponse(user)
        ));
    }

    // 获取当前用户信息
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("message", "未提供有效的认证令牌"));
        }

        String token = authHeader.substring(7);
        Long userId = tokenService.verifyToken(token);

        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "令牌无效或已过期"));
        }

        // 从缓存获取用户信息
        User user = getUserFromCache(userId);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "用户信息不存在"));
        }

        return ResponseEntity.ok(buildUserResponse(user));
    }
    //============================私有方法
    // 构建认证响应
    private ResponseEntity<?> buildAuthResponse(String token, User user) {
        return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "displayName", user.getDisplayName(),
                "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "/uploads/images/default/touxiang.jpg"
        ));
    }

    // 构建用户信息响应
    private Map<String, Object> buildUserResponse(User user) {
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "displayName", user.getDisplayName(),
                "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "/uploads/images/default/touxiang.jpg",
                "createdAt", user.getCreatedAt()
        );
    }

    // 缓存用户信息
    private void cacheUserInfo(User user) {
        try {
            String userKey = USER_INFO_PREFIX + user.getId();
            String sessionKey = USER_SESSION_PREFIX + user.getId();

            // 缓存用户基本信息
            redisUtil.set(userKey, buildUserResponse(user), SESSION_EXPIRE_HOURS, TimeUnit.HOURS);

            // 缓存用户会话状态
            Map<String, Object> sessionInfo = Map.of(
                    "userId", user.getId(),
                    "username", user.getUsername(),
                    "role", user.getRole().name(),
                    "lastLogin", System.currentTimeMillis()
            );
            redisUtil.set(sessionKey, sessionInfo, SESSION_EXPIRE_HOURS, TimeUnit.HOURS);

            System.out.println("✅ 用户信息已缓存: " + user.getId());
        } catch (Exception e) {
            System.out.println("❌ 缓存用户信息失败: " + e.getMessage());
        }
    }

    // 从缓存获取用户信息
    private User getUserFromCache(Long userId) {
        try {
            String userKey = USER_INFO_PREFIX + userId;
            Object cachedUser = redisUtil.get(userKey);

            if (cachedUser != null) {
                System.out.println("✅ 从Redis缓存获取用户信息: " + userId);

                // 将缓存的Map转换为User对象
                @SuppressWarnings("unchecked")
                Map<String, Object> userMap = (Map<String, Object>) cachedUser;

                // 从数据库获取完整的User对象，而不是手动创建
                return userService.findById(userId).orElse(null);
            }
        } catch (Exception e) {
            System.out.println("❌ 从缓存获取用户信息失败: " + e.getMessage());
        }

        // 缓存中没有，从数据库获取
        System.out.println("🔍 从数据库获取用户信息: " + userId);
        return userService.findById(userId).orElse(null);
    }

    // 清除用户缓存
    private void clearUserCache(Long userId) {
        try {
            String userKey = USER_INFO_PREFIX + userId;
            String sessionKey = USER_SESSION_PREFIX + userId;

            redisUtil.delete(userKey);
            redisUtil.delete(sessionKey);

            System.out.println("✅ 用户缓存已清除: " + userId);
        } catch (Exception e) {
            System.out.println("❌ 清除用户缓存失败: " + e.getMessage());
        }
    }

}

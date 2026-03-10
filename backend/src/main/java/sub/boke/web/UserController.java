package sub.boke.web;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sub.boke.domain.User;
import sub.boke.repository.ArticleRepository;
import sub.boke.repository.UserRepository;
import sub.boke.service.FileStorageService;
import sub.boke.service.UserService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final ArticleRepository articleRepository;
    private final UserService userService;

    public UserController(UserRepository userRepository, FileStorageService fileStorageService, ArticleRepository articleRepository, UserService userService) {
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.articleRepository = articleRepository;
        this.userService = userService;
    }

    public record ProfileUpdateReq(String displayName, String signature) {
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdateReq req, @AuthenticationPrincipal User user) {
        user.setDisplayName(req.displayName());
        user.setSignature(req.signature());
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "已更新"));
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> updateAvatar(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal User user)
            throws IOException {
        String url = "/uploads" + fileStorageService.saveImage(file);
        user.setAvatarUrl(url);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("avatarUrl", url));
    }

    // 获取主页背景图片
    @GetMapping("/home-background")
    public ResponseEntity<?> getHomeBackground(@AuthenticationPrincipal User user) {
        try {
            // 返回用户的背景图片URL，如果没有设置则返回null
            String backgroundUrl = user.getHomeBackgroundUrl();
            return ResponseEntity.ok(Map.of("homeBackgroundUrl", backgroundUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取背景图片失败: " + e.getMessage()));
        }
    }

    // 获取用户信息
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserInfo(@PathVariable Long userId, @AuthenticationPrincipal User currentUser) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("displayName", user.getDisplayName());
            userInfo.put("avatarUrl", user.getAvatarUrl());
            userInfo.put("signature", user.getSignature());
            userInfo.put("role", user.getRole());
            userInfo.put("homeBackgroundUrl", user.getHomeBackgroundUrl());

            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取用户信息失败: " + e.getMessage()));
        }
    }

    // 获取用户统计信息
    @GetMapping("/{userId}/stats")
    public ResponseEntity<?> getUserStats(@PathVariable Long userId) {
        try {
            // 获取文章数量
            long articleCount = articleRepository.countByAuthorIdAndPublishedTrue(userId);

            // 获取总点赞数
            long likeCount = articleRepository.sumLikeCountByAuthorId(userId);

            // 获取总阅读数
            long viewCount = articleRepository.sumViewCountByAuthorId(userId);

            Map<String, Object> stats = new HashMap<>();
            stats.put("articleCount", articleCount);
            stats.put("likeCount", likeCount);
            stats.put("viewCount", viewCount);

            System.out.println("🔍 用户统计 - 用户ID: " + userId +
                    ", 文章数: " + articleCount +
                    ", 点赞数: " + likeCount +
                    ", 阅读数: " + viewCount);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("❌ 获取用户统计失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", "获取用户统计失败: " + e.getMessage()));
        }
    }

    // 更新用户主页背景
    @PostMapping("/home-background")
    public ResponseEntity<?> updateHomeBackground(@RequestParam("file") MultipartFile file,
                                                  @AuthenticationPrincipal User user) {
        try {
            // 文件验证
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "文件不能为空"));
            }
            if (!file.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("message", "只能上传图片文件"));
            }
            // 保存图片并获取路径
            String savePath = fileStorageService.saveImage(file);
            String backgroundUrl = "/uploads" + savePath;

            // 更新用户信息
            user.setHomeBackgroundUrl(backgroundUrl);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "背景图片更新成功",
                    "homeBackgroundUrl", backgroundUrl
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "更新背景图片失败: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);

            // todo
            //存在安全隐患：搜索可以匹配到用户的登录的名称username
            //正确的做法应该是：搜索匹配的是用户的昵称display_name

            Page<User> users = userRepository.searchUsers(keyword, pageable);
//            Page<User> users = userRepository.findByUsernameContainingOrDisplayNameContaining(keyword, pageable);

            // 转换为前端需要的格式
            Page<Map<String, Object>> result = users.map(user -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("username", user.getUsername());
                userMap.put("displayName", user.getDisplayName());
                userMap.put("avatarUrl", user.getAvatarUrl());
                userMap.put("signature", user.getSignature());
                userMap.put("role", user.getRole());
                return userMap;
            });

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "搜索用户失败: " + e.getMessage()));
        }
    }

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean exists = userService.findByUsername(username).isPresent();
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}

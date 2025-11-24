package sub.boke.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sub.boke.domain.Article;
import sub.boke.domain.User;
import sub.boke.service.ArticleService;
import sub.boke.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('BLOGGER')")  // 改为 BLOGGER 角色
public class AdminController {

    private final UserService userService;
    private final ArticleService articleService;

    public AdminController(UserService userService, ArticleService articleService) {
        this.userService = userService;
        this.articleService = articleService;
    }

    // 用户管理 API
    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userService.findAllUsers(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("users", users.getContent());
        response.put("currentPage", users.getNumber());
        response.put("totalItems", users.getTotalElements());
        response.put("totalPages", users.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        try {
            String role = request.get("role");
            User.Role userRole = User.Role.valueOf(role.toUpperCase());

            User user = userService.updateUserRole(userId, userRole);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "用户角色更新成功");
            response.put("user", user);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "无效的角色类型"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(Map.of("message", "用户删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 文章管理 API
    @GetMapping("/articles")
    public ResponseEntity<?> getArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Article> articles = articleService.findAllArticles(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("articles", articles.getContent());
        response.put("currentPage", articles.getNumber());
        response.put("totalItems", articles.getTotalElements());
        response.put("totalPages", articles.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/articles/{articleId}")
    public ResponseEntity<?> deleteArticle(@PathVariable Long articleId) {
        try {
            articleService.deleteArticle(articleId);
            return ResponseEntity.ok(Map.of("message", "文章删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/articles/{articleId}/toggle-publish")
    public ResponseEntity<?> toggleArticlePublish(@PathVariable Long articleId) {
        try {
            Article article = articleService.togglePublishStatus(articleId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", article.isPublished() ? "文章已发布" : "文章已取消发布");
            response.put("article", article);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 创建博主账号 API
    @PostMapping("/create-blogger")
    public ResponseEntity<?> createBlogger(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String email = request.get("email");
            String password = request.get("password");
            String displayName = request.get("displayName");

            if (username == null || email == null || password == null || displayName == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "所有字段都不能为空"));
            }

            // 检查用户名是否已存在
            if (userService.findByUsername(username).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "用户名已存在"));
            }

            // 检查邮箱是否已存在
            if (userService.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "邮箱已被注册"));
            }

            // 使用新的register方法，包含邮箱参数
            User user = userService.register(username, email, password, User.Role.BLOGGER, displayName);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "博主账号创建成功");
            response.put("user", user);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
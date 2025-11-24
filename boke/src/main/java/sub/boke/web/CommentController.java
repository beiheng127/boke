package sub.boke.web;

import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sub.boke.domain.Article;
import sub.boke.domain.Comment;
import sub.boke.domain.User;
import sub.boke.repository.ArticleRepository;
import sub.boke.repository.CommentRepository;
import sub.boke.util.RedisUtil;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    @Autowired
    private RedisUtil redisUtil;
    private static final String ARTICLE_CACHE_PREFIX = "article:";
    private static final String ARTICLE_LIST_CACHE_PREFIX = "article_list:";

    public CommentController(CommentRepository commentRepository, ArticleRepository articleRepository) {
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
    }

    public record CreateReq(Long articleId, @NotBlank String content) {
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateReq req, @AuthenticationPrincipal User user) {
        try {
            Article article = articleRepository.findById(req.articleId()).orElse(null);
            if (article == null)
                return ResponseEntity.badRequest().body(Map.of("message", "文章不存在"));
            // 明确设置创建时间
            Comment c = Comment.builder()
                    .article(article)
                    .user(user)
                    .content(req.content())
                    .createdAt(Instant.now()) // 明确设置时间
                    .build();
            c = commentRepository.save(c);

            clearArticleCache(article.getId());

            return ResponseEntity.ok(Map.of("id", c.getId()));
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", "创建评论失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        boolean isBlogger = user.getRole() == User.Role.BLOGGER;
        return commentRepository.findById(id)
                .filter(c -> isBlogger || c.getUser().getId().equals(user.getId())
                        || c.getArticle().getAuthor().getId().equals(user.getId()))
                .map(c -> {
                    Long articleId = c.getArticle().getId();
                    commentRepository.deleteById(id);
                    clearArticleCache(articleId);
                    return ResponseEntity.ok(Map.of("message", "已删除"));
                })
                .orElseGet(() -> ResponseEntity.status(403).body(Map.of("message", "无权限或评论不存在")));
    }

    @GetMapping("/by-article/{articleId}")
    public ResponseEntity<?> listByArticle(@PathVariable Long articleId,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Map<String, Object>> commentsPage = commentRepository
                    .findByArticle_IdOrderByCreatedAtAsc(articleId, PageRequest.of(page, size))
                    .map(c -> {
                        try {
                            return Map.of(
                                    "id", c.getId(),
                                    "user", c.getUser() != null ? c.getUser().getUsername() : "未知用户",
                                    "content", c.getContent() != null ? c.getContent() : "",
                                    "createdAt", c.getCreatedAt() != null ? c.getCreatedAt() : Instant.now());
                        } catch (Exception e) {
                            // 防止单个评论序列化失败影响整个列表
                            return Map.of(
                                    "id", c.getId(),
                                    "user", "用户信息加载失败",
                                    "content", c.getContent() != null ? c.getContent() : "",
                                    "createdAt", Instant.now());
                        }
                    });

            return ResponseEntity.ok(commentsPage);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", "加载评论失败: " + e.getMessage()));
        }
    }

    private void clearArticleCache(Long articleId) {
        try {
            // 清除文章详情缓存
            redisUtil.delete(ARTICLE_CACHE_PREFIX + articleId);

            // 清除所有相关的文章列表缓存
            Set<String> listKeys = redisUtil.keys(ARTICLE_LIST_CACHE_PREFIX + "*");
            if (listKeys != null && !listKeys.isEmpty()) {
                redisUtil.delete(listKeys); // 使用新的批量删除方法
                System.out.println("✅ 清除文章列表缓存，数量: " + listKeys.size());
            }
        } catch (Exception e) {
            System.out.println("❌ 清除文章缓存失败: " + e.getMessage());
        }
    }
}

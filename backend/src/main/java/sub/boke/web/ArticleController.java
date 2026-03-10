// ArticleController.java - 添加浏览量相关功能
package sub.boke.web;

import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sub.boke.domain.Article;
import sub.boke.domain.User;
import sub.boke.repository.ArticleLikeRepository;
import sub.boke.repository.ArticleRepository;
import sub.boke.repository.CommentRepository;
import sub.boke.repository.FavoriteRepository;
import sub.boke.service.ArticleService;
import sub.boke.service.FileStorageService;
import sub.boke.util.RedisUtil;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {
    private final ArticleService articleService;
    private final FileStorageService fileStorageService;
    private final ArticleLikeRepository likeRepository;
    private final FavoriteRepository favoriteRepository;
    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;

    @Autowired
    private RedisUtil redisUtil;
    private static final String ARTICLE_CACHE_PREFIX = "article:";
    private static final String ARTICLE_LIST_CACHE_PREFIX = "article_list:";


    public ArticleController(ArticleService articleService, FileStorageService fileStorageService,
                             ArticleLikeRepository likeRepository, FavoriteRepository favoriteRepository,
                             CommentRepository commentRepository, ArticleRepository articleRepository) {
        this.articleService = articleService;
        this.fileStorageService = fileStorageService;
        this.likeRepository = likeRepository;
        this.favoriteRepository = favoriteRepository;
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
    }

    public record CreateArticleRequest(@NotBlank String title, String summary, @NotBlank String content,
                                       String coverImageUrl, Boolean published) {
    }

    // 修改：创建文章接口 - 支持发布状态
    @PostMapping
    @CacheEvict(value = {"article_lists"}, allEntries = true)
    public ResponseEntity<?> create(@RequestBody CreateArticleRequest req, @AuthenticationPrincipal User user) {
        try {
            Article article = Article.builder()
                    .title(req.title())
                    .summary(req.summary())
                    .content(req.content())
                    .coverImageUrl(req.coverImageUrl())
                    .author(user)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .viewCount(0)
                    .published(req.published() != null ? req.published() : false) // 默认为草稿
                    .build();

            Article savedArticle = articleRepository.save(article);
            clearAllArticleListCache();
            return ResponseEntity.ok(Map.of("id", savedArticle.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "创建文章失败: " + e.getMessage()));
        }
    }

    // 修改：更新文章接口 - 支持发布状态
    @PutMapping("/{id}")
    @CacheEvict(value = {"articles", "article_lists"}, allEntries = true)
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CreateArticleRequest req,
                                    @AuthenticationPrincipal User user) {
        boolean isBlogger = user.getRole() == User.Role.BLOGGER;

        return articleService.findById(id).filter(a -> isBlogger || a.getAuthor().getId().equals(user.getId()))
                .map(a -> {
                    a.setTitle(req.title());
                    a.setSummary(req.summary());
                    a.setContent(req.content());
                    a.setCoverImageUrl(req.coverImageUrl());
                    a.setUpdatedAt(Instant.now());
                    // 如果请求中包含发布状态，则更新
                    if (req.published() != null) {
                        a.setPublished(req.published());
                    }
                    articleRepository.save(a);

                    // 清除相关缓存
                    redisUtil.delete(ARTICLE_CACHE_PREFIX + id);
                    System.out.println("✅ 清除文章缓存: " + id);

                    return ResponseEntity.ok(Map.of("message", "已更新"));
                })
                .orElseGet(() -> ResponseEntity.status(403).body(Map.of("message", "无权限或文章不存在")));
    }


    @DeleteMapping("/{id}")
    @CacheEvict(value = {"articles", "article_lists"}, allEntries = true)
    public ResponseEntity<?> delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        boolean isBlogger = user.getRole() == User.Role.BLOGGER;
        return articleService.findById(id).filter(a -> isBlogger || a.getAuthor().getId().equals(user.getId()))
                .map(a -> {
                    articleService.deleteById(id);

                    // 清除相关缓存
                    redisUtil.delete(ARTICLE_CACHE_PREFIX + id);
                    System.out.println("✅ 清除文章缓存: " + id);

                    return ResponseEntity.ok(Map.of("message", "已删除"));
                })
                .orElseGet(() -> ResponseEntity.status(403).body(Map.of("message", "无权限或文章不存在")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id, @AuthenticationPrincipal User user) {
        String cacheKey = ARTICLE_CACHE_PREFIX + id;
        Object cachedArticle = redisUtil.get(cacheKey);
        if (cachedArticle != null) {
            System.out.println("✅ 从Redis缓存获取文章: " + id);
            return ResponseEntity.ok(cachedArticle);
        }

        Optional<Article> articleOpt = articleService.findById(id);
        if (articleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Article article = articleOpt.get();

        // 检查文章发布状态和权限
        if (!article.isPublished()) {
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("message", "文章不存在"));
            }

            boolean isAuthor = article.getAuthor().getId().equals(user.getId());
            boolean isBlogger = user.getRole() == User.Role.BLOGGER;

            if (!isAuthor && !isBlogger) {
                return ResponseEntity.status(404).body(Map.of("message", "文章不存在"));
            }
        }

        // 增加浏览量（只对已发布的文章）
        if (article.isPublished()) {
            articleService.incrementViewCount(id);
            redisUtil.delete(cacheKey);
            articleOpt = articleService.findById(id);
            if (articleOpt.isPresent()) {
                article = articleOpt.get();
            }
        }

        // 构建返回数据
        Map<String, Object> result = buildArticleResponse(article, user);

        // 存入缓存，缓存30分钟
        redisUtil.set(cacheKey, result, 30, TimeUnit.MINUTES);
        System.out.println("✅ 文章存入Redis缓存: " + id);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String path = fileStorageService.saveImage(file);
        return ResponseEntity.ok(Map.of("url", "/uploads" + path));
    }

    // 新增：获取文章统计数据
    @GetMapping("/{id}/stats")
    public ResponseEntity<?> getArticleStats(@PathVariable Long id) {
        return articleService.findById(id)
                .<ResponseEntity<?>>map(a -> ResponseEntity.ok(Map.of(
                        "viewCount", a.getViewCount() != null ? a.getViewCount() : 0,
                        "likeCount", likeRepository.countByArticle_Id(a.getId()),
                        "favCount", favoriteRepository.countByArticle_Id(a.getId())
                )))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping
    public ResponseEntity<?> listArticles(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "hot") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long authorId,
            @AuthenticationPrincipal User user) {

        // 构建缓存键
        String cacheKey = ARTICLE_LIST_CACHE_PREFIX +
                (authorId != null ? "author:" + authorId + ":" : "") +
                keyword + ":" + sortBy + ":" + page + ":" + size;

        // 尝试从缓存获取
        Object cachedResult = redisUtil.get(cacheKey);
        if (cachedResult != null) {
            System.out.println("✅ 从Redis缓存获取文章列表");
            return ResponseEntity.ok(cachedResult);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Article> articles = articleService.findByKeywordAndAuthor(keyword, authorId, sortBy, pageable);

        // 为每个文章添加交互状态和热度分数
        Page<Map<String, Object>> result = articles.map(article -> buildArticleMap(article, user));

        // 缓存业务数据，不是 ResponseEntity
        redisUtil.set(cacheKey, result, 30, TimeUnit.MINUTES);
        System.out.println("✅ 文章列表存入Redis缓存");

        return ResponseEntity.ok(result);
    }

    // 新增：获取当前用户的草稿列表
    @GetMapping("/drafts")
    public ResponseEntity<?> getDrafts(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "未登录"));
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Article> drafts = articleService.findDraftsByAuthor(user, pageable);

        // 转换为前端需要的格式
        Page<Map<String, Object>> result = drafts.map(article -> buildArticleMapForDraft(article));


        return ResponseEntity.ok(result);
    }

    // 新增：切换文章发布状态
    @PostMapping("/{id}/toggle-publish")
    @CacheEvict(value = {"articles", "article_lists"}, allEntries = true)
    public ResponseEntity<?> togglePublish(@PathVariable Long id, @AuthenticationPrincipal User user) {
        try {
            // 检查权限
            Optional<Article> articleOpt = articleService.findById(id);
            if (articleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Article article = articleOpt.get();
            boolean isAuthor = article.getAuthor().getId().equals(user.getId());
            boolean isBlogger = user.getRole() == User.Role.BLOGGER;

            if (!isAuthor && !isBlogger) {
                return ResponseEntity.status(403).body(Map.of("message", "无权限操作此文章"));
            }

            Article updatedArticle = articleService.togglePublishStatus(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", updatedArticle.isPublished() ? "文章已发布" : "文章已转为草稿");
            response.put("article", updatedArticle);
            response.put("published", updatedArticle.isPublished());

            redisUtil.delete(ARTICLE_CACHE_PREFIX + id);
            System.out.println("✅ 清除文章缓存: " + id);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }


    private Map<String, Object> buildArticleResponse(Article article, User user) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", article.getId());
        result.put("title", article.getTitle() != null ? article.getTitle() : "");
        result.put("summary", article.getSummary() != null ? article.getSummary() : "");
        result.put("content", article.getContent() != null ? article.getContent() : "");
        result.put("coverImageUrl", article.getCoverImageUrl() != null ? article.getCoverImageUrl() : "");
        result.put("author", article.getAuthor() != null ? article.getAuthor().getUsername() : "未知作者");
        result.put("authorId", article.getAuthor() != null ? article.getAuthor().getId() : null);
        result.put("createdAt", article.getCreatedAt() != null ? article.getCreatedAt() : Instant.now());
        result.put("updatedAt", article.getUpdatedAt() != null ? article.getUpdatedAt() : Instant.now());
        result.put("published", article.isPublished());
        result.put("viewCount", article.getViewCount() != null ? article.getViewCount() : 0);
        result.put("likeCount", likeRepository.countByArticle_Id(article.getId()));
        result.put("favCount", favoriteRepository.countByArticle_Id(article.getId()));

        if (user != null) {
            boolean liked = likeRepository.findByUser_IdAndArticle_Id(user.getId(), article.getId()).isPresent();
            boolean favorited = favoriteRepository.findByUser_IdAndArticle_Id(user.getId(), article.getId()).isPresent();
            result.put("liked", liked);
            result.put("favorited", favorited);
        } else {
            result.put("liked", false);
            result.put("favorited", false);
        }

        return result;
    }

    // 构建文章列表项数据
    private Map<String, Object> buildArticleMap(Article article, User user) {
        Map<String, Object> articleMap = new HashMap<>();
        articleMap.put("id", article.getId());
        articleMap.put("title", article.getTitle());
        articleMap.put("summary", article.getSummary());
        articleMap.put("coverImageUrl", article.getCoverImageUrl());
        articleMap.put("createdAt", article.getCreatedAt());
        articleMap.put("updatedAt", article.getUpdatedAt());
        articleMap.put("viewCount", article.getViewCount());
        articleMap.put("author", article.getAuthor());
        articleMap.put("authorId", article.getAuthor().getId());
        articleMap.put("published", article.isPublished());

        // 获取文章的统计数据
        Map<String, Object> stats = articleService.getArticleStats(article.getId());
        long viewCount = article.getViewCount() != null ? article.getViewCount() : 0;
        long likeCount = safeGetLong(stats.get("likeCount"));
        long commentCount = safeGetLong(stats.get("commentCount"));
        long favCount = safeGetLong(stats.get("favCount"));

        // 计算热度分数用于显示
        double timeFactor = calculateTimeFactor(article.getCreatedAt());
        double hotScore = 0.3 * viewCount + 0.2 * likeCount + 0.2 * commentCount + 0.2 * favCount + 0.1 * timeFactor;
        articleMap.put("hotScore", Math.round(hotScore * 100.0) / 100.0);

        // 添加统计数据到返回结果
        articleMap.put("likeCount", likeCount);
        articleMap.put("favCount", favCount);
        articleMap.put("commentCount", commentCount);

        // 添加交互状态
        if (user != null) {
            Map<String, Object> interaction = articleService.getUserInteractionStatus(article.getId(), user.getId());
            articleMap.put("liked", interaction.get("liked"));
            articleMap.put("favorited", interaction.get("favorited"));
        } else {
            articleMap.put("liked", false);
            articleMap.put("favorited", false);
        }

        return articleMap;
    }

    // 构建草稿文章数据
    private Map<String, Object> buildArticleMapForDraft(Article article) {
        Map<String, Object> articleMap = new HashMap<>();
        articleMap.put("id", article.getId());
        articleMap.put("title", article.getTitle());
        articleMap.put("summary", article.getSummary());
        articleMap.put("content", article.getContent());
        articleMap.put("coverImageUrl", article.getCoverImageUrl());
        articleMap.put("createdAt", article.getCreatedAt());
        articleMap.put("updatedAt", article.getUpdatedAt());
        articleMap.put("viewCount", article.getViewCount());
        articleMap.put("author", article.getAuthor());
        articleMap.put("authorId", article.getAuthor().getId());
        articleMap.put("published", article.isPublished());
        return articleMap;
    }

    // 辅助方法：安全获取Long值
    private long safeGetLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        return 0L;
    }

    // 计算时间因子（与ArticleService中保持一致）
    private double calculateTimeFactor(Instant createdAt) {
        if (createdAt == null) return 0;

        try {
            LocalDateTime createTime = LocalDateTime.ofInstant(createdAt, ZoneId.systemDefault());
            LocalDateTime now = LocalDateTime.now();
            long hoursSinceCreation = java.time.Duration.between(createTime, now).toHours();
            return Math.max(0, 100 - hoursSinceCreation * 0.1);
        } catch (Exception e) {
            return 0;
        }
    }
    private void clearAllArticleListCache() {
        try {
            Set<String> listKeys = redisUtil.keys(ARTICLE_LIST_CACHE_PREFIX + "*");
            if (listKeys != null && !listKeys.isEmpty()) {
                redisUtil.delete(listKeys); // 使用新的批量删除方法
                System.out.println("✅ 清除所有文章列表缓存，数量: " + listKeys.size());
            }
        } catch (Exception e) {
            System.out.println("❌ 清除文章列表缓存失败: " + e.getMessage());
        }
    }

}
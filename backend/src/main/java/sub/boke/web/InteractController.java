package sub.boke.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sub.boke.domain.Article;
import sub.boke.domain.ArticleLike;
import sub.boke.domain.Favorite;
import sub.boke.domain.User;
import sub.boke.repository.ArticleLikeRepository;
import sub.boke.repository.ArticleRepository;
import sub.boke.repository.FavoriteRepository;
import sub.boke.service.ArticleService;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/interact")
public class InteractController {
    private final ArticleRepository articleRepository;
    private final ArticleLikeRepository likeRepository;
    private final FavoriteRepository favoriteRepository;
    private final ArticleService articleService;

    public InteractController(ArticleRepository articleRepository,
                              ArticleLikeRepository likeRepository,
                              FavoriteRepository favoriteRepository,
                              ArticleService articleService) {
        this.articleRepository = articleRepository;
        this.likeRepository = likeRepository;
        this.favoriteRepository = favoriteRepository;
        this.articleService = articleService;
    }

    @PostMapping("/like/{articleId}")
    public ResponseEntity<?> like(@PathVariable Long articleId, @AuthenticationPrincipal User user) {
        Article article = articleRepository.findById(articleId).orElse(null);
        if (article == null)
            return ResponseEntity.notFound().build();

        return likeRepository.findByUser_IdAndArticle_Id(user.getId(), articleId)
                .map(already -> {
                    likeRepository.delete(already);
                    // 返回更新后的完整状态
                    Map<String, Object> stats = articleService.getUserInteractionStatus(articleId, user.getId());
                    return ResponseEntity.ok(Map.of(
                            "liked", false,
                            "likeCount", stats.get("likeCount"),
                            "favCount", stats.get("favCount"),
                            "favorited", stats.get("favorited")
                    ));
                })
                .orElseGet(() -> {
                    // 确保正确创建 ArticleLike 对象
                    ArticleLike like = ArticleLike.builder()
                            .article(article)
                            .user(user)
                            .createdAt(Instant.now()) // 显式设置时间
                            .build();
                    likeRepository.save(like);

                    // 返回更新后的完整状态
                    Map<String, Object> stats = articleService.getUserInteractionStatus(articleId, user.getId());
                    return ResponseEntity.ok(Map.of(
                            "liked", true,
                            "likeCount", stats.get("likeCount"),
                            "favCount", stats.get("favCount"),
                            "favorited", stats.get("favorited")
                    ));
                });
    }

    @PostMapping("/fav/{articleId}")
    public ResponseEntity<?> favorite(@PathVariable Long articleId, @AuthenticationPrincipal User user) {
        Article article = articleRepository.findById(articleId).orElse(null);
        if (article == null)
            return ResponseEntity.notFound().build();

        return favoriteRepository.findByUser_IdAndArticle_Id(user.getId(), articleId)
                .map(already -> {
                    favoriteRepository.delete(already);
                    // 返回更新后的完整状态
                    Map<String, Object> stats = articleService.getUserInteractionStatus(articleId, user.getId());
                    return ResponseEntity.ok(Map.of(
                            "favorited", false,
                            "likeCount", stats.get("likeCount"),
                            "favCount", stats.get("favCount"),
                            "liked", stats.get("liked")
                    ));
                })
                .orElseGet(() -> {
                    // 确保正确创建 Favorite 对象
                    Favorite favorite = Favorite.builder()
                            .article(article)
                            .user(user)
                            .createdAt(Instant.now()) // 显式设置时间
                            .build();
                    favoriteRepository.save(favorite);

                    // 返回更新后的完整状态
                    Map<String, Object> stats = articleService.getUserInteractionStatus(articleId, user.getId());
                    return ResponseEntity.ok(Map.of(
                            "favorited", true,
                            "likeCount", stats.get("likeCount"),
                            "favCount", stats.get("favCount"),
                            "liked", stats.get("liked")
                    ));
                });
    }

    @GetMapping("/favorites")
    public Page<Map<String, Object>> myFavorites(@AuthenticationPrincipal User user,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        return favoriteRepository.findByUser_IdOrderByCreatedAtDesc(user.getId(), PageRequest.of(page, size))
                .map(f -> {
                    Long articleId = f.getArticle().getId();
                    Map<String, Object> stats = articleService.getArticleStats(articleId);

                    return Map.of(
                            "articleId", articleId,
                            "title", f.getArticle().getTitle(),
                            "summary", f.getArticle().getSummary(),
                            "coverImageUrl", f.getArticle().getCoverImageUrl(),
                            "createdAt", f.getArticle().getCreatedAt(),
                            "viewCount", f.getArticle().getViewCount(),
                            "likeCount", stats.get("likeCount"),
                            "favCount", stats.get("favCount")
                    );
                });
    }

    // 获取用户对文章的交互状态
    @GetMapping("/status/{articleId}")
    public ResponseEntity<?> getInteractionStatus(@PathVariable Long articleId, @AuthenticationPrincipal User user) {
        Map<String, Object> status = articleService.getUserInteractionStatus(articleId, user.getId());
        return ResponseEntity.ok(status);
    }

    // 批量获取文章的状态信息（用于文章列表）
    @GetMapping("/batch-status")
    public ResponseEntity<?> getBatchInteractionStatus(@RequestParam Long[] articleIds, @AuthenticationPrincipal User user) {
        Map<Long, Map<String, Object>> result = new java.util.HashMap<>();

        for (Long articleId : articleIds) {
            result.put(articleId, articleService.getUserInteractionStatus(articleId, user.getId()));
        }

        return ResponseEntity.ok(result);
    }
}
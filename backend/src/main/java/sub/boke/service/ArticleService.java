// ArticleService.java - 修复版本
package sub.boke.service;

import lombok.Data;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sub.boke.domain.Article;
import sub.boke.domain.User;
import sub.boke.repository.ArticleLikeRepository;
import sub.boke.repository.ArticleRepository;
import sub.boke.repository.CommentRepository;
import sub.boke.repository.FavoriteRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleLikeRepository likeRepository;
    private final FavoriteRepository favoriteRepository;
    private final CommentRepository commentRepository;

    public ArticleService(ArticleRepository articleRepository,
                          ArticleLikeRepository likeRepository,
                          FavoriteRepository favoriteRepository,
                          CommentRepository commentRepository) {
        this.articleRepository = articleRepository;
        this.likeRepository = likeRepository;
        this.favoriteRepository = favoriteRepository;
        this.commentRepository = commentRepository;
    }

    // 修改 ArticleService.java 中的 findByKeyword 方法
    public Page<Article> findByKeyword(String keyword, String sortBy, Pageable pageable) {
        Page<Article> articles;

        if (keyword == null || keyword.trim().isEmpty()) {
            // 无关键词搜索，只查询已发布的文章
            switch (sortBy) {
                case "time": // 最新发布 - 时间倒序
                    articles = articleRepository.findByPublishedTrueOrderByCreatedAtDesc(pageable);
                    break;
                case "oldest": // 最旧发布 - 时间正序
                    articles = articleRepository.findByPublishedTrueOrderByCreatedAtAsc(pageable);
                    break;
                case "hot": // 热度排序 - 先获取所有已发布的，后面内存排序
                    articles = articleRepository.findByPublishedTrue(pageable);
                    break;
                default: // 默认按最新发布
                    articles = articleRepository.findByPublishedTrueOrderByCreatedAtDesc(pageable);
                    break;
            }
        } else {
            // 有关键词搜索，只搜索已发布的文章
            switch (sortBy) {
                case "time": // 最新发布 - 时间倒序
                    articles = articleRepository.findByTitleContainingOrSummaryContainingAndPublishedTrueOrderByCreatedAtDesc(keyword, pageable);
                    break;
                case "oldest": // 最旧发布 - 时间正序
                    articles = articleRepository.findByTitleContainingOrSummaryContainingAndPublishedTrueOrderByCreatedAtAsc(keyword, pageable);
                    break;
                case "hot": // 热度排序 - 先按时间倒序获取已发布的，后面内存排序
                    articles = articleRepository.findByTitleContainingOrSummaryContainingAndPublishedTrueOrderByCreatedAtDesc(keyword, pageable);
                    break;
                default: // 默认按最新发布
                    articles = articleRepository.findByTitleContainingOrSummaryContainingAndPublishedTrueOrderByCreatedAtDesc(keyword, pageable);
                    break;
            }
        }

        // 如果是热度排序，在内存中重新排序
        if ("hot".equals(sortBy)) {
            return sortArticlesByHotScore(articles, pageable);
        }

        return articles;
    }

    // 新增：获取用户的草稿列表
    public Page<Article> findDraftsByAuthor(User author, Pageable pageable) {
        return articleRepository.findByAuthorAndPublishedFalseOrderByUpdatedAtDesc(author, pageable);
    }

    // 新增：根据热度分数排序文章
    private Page<Article> sortArticlesByHotScore(Page<Article> articles, Pageable pageable) {
        List<Article> articleList = articles.getContent();

        // 为每篇文章计算热度分数
        List<ArticleWithScore> scoredArticles = articleList.stream()
                .map(article -> {
                    double hotScore = calculateHotScore(article);
                    return new ArticleWithScore(article, hotScore);
                })
                .sorted((a1, a2) -> Double.compare(a2.score, a1.score)) // 按热度降序排序
                .collect(Collectors.toList());

        // 转换回 Article 列表
        List<Article> sortedArticles = scoredArticles.stream()
                .map(ArticleWithScore::getArticle)
                .collect(Collectors.toList());

        // 返回新的 Page 对象
        return new PageImpl<>(sortedArticles, pageable, articles.getTotalElements());
    }

    // 新增：计算热度分数 - 修复版本
    private double calculateHotScore(Article article) {
        try {
            // 获取各项数据
            long viewCount = article.getViewCount() != null ? article.getViewCount() : 0;
            long likeCount = likeRepository.countByArticle_Id(article.getId());
            long favCount = favoriteRepository.countByArticle_Id(article.getId());
            long commentCount = commentRepository.countByArticleId(article.getId());

            System.out.println("文章 " + article.getId() + " 统计: 浏览=" + viewCount +
                    ", 点赞=" + likeCount + ", 收藏=" + favCount + ", 评论=" + commentCount);

            // 计算时间因子（新文章分数更高）
            double timeFactor = calculateTimeFactor(article.getCreatedAt());

            // 热度计算公式：0.3*浏览数 + 0.2*点赞数 + 0.2*评论数 + 0.2*收藏数 + 0.1*时间因子
            double hotScore = 0.3 * viewCount +
                    0.2 * likeCount +
                    0.2 * commentCount +
                    0.2 * favCount +
                    0.1 * timeFactor;

            System.out.println("文章 " + article.getId() + " 热度分数: " + hotScore);
            return hotScore;

        } catch (Exception e) {
            System.err.println("计算文章 " + article.getId() + " 热度分数时出错: " + e.getMessage());
            return 0.0;
        }
    }

    // 新增：计算时间因子
    private double calculateTimeFactor(Instant createdAt) {
        if (createdAt == null) {
            return 0;
        }

        try {
            // 计算文章存在的小时数
            LocalDateTime createTime = LocalDateTime.ofInstant(createdAt, ZoneId.systemDefault());
            LocalDateTime now = LocalDateTime.now();
            long hoursSinceCreation = java.time.Duration.between(createTime, now).toHours();

            // 使用衰减函数：新文章分数高，旧文章分数低
            // 100分基础，每小时衰减0.1分
            return Math.max(0, 100 - hoursSinceCreation * 0.1);
        } catch (Exception e) {
            return 0;
        }
    }

    // 辅助类：用于存储文章和对应的热度分数
    @Data
    private static class ArticleWithScore {
        private final Article article;
        private final double score;
    }

    // 原有的方法保持不变
    public Optional<Article> findById(Long id) {
        return articleRepository.findWithAuthorById(id);
    }

    public Article create(String title, String summary, String content, String coverImageUrl, User author, boolean published) {
        Article article = Article.builder()
                .title(title)
                .summary(summary)
                .content(content)
                .coverImageUrl(coverImageUrl)
                .author(author)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .viewCount(0)
                .published(published)  // 设置发布状态
                .build();
        return articleRepository.save(article);
    }

    public Optional<Article> update(Long id, String title, String summary, String content, String coverImageUrl) {
        return articleRepository.findById(id).map(article -> {
            article.setTitle(title);
            article.setSummary(summary);
            article.setContent(content);
            article.setCoverImageUrl(coverImageUrl);
            article.setUpdatedAt(Instant.now());
            return articleRepository.save(article);
        });
    }

    public void deleteById(Long id) {
        articleRepository.deleteById(id);
    }

    public Page<Article> findAllArticles(Pageable pageable) {
        return articleRepository.findAll(pageable);
    }

    public Article togglePublishStatus(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
        article.setPublished(!article.isPublished());
        article.setUpdatedAt(Instant.now());
        return articleRepository.save(article);
    }

    public void deleteArticle(Long articleId) {
        if (!articleRepository.existsById(articleId)) {
            throw new RuntimeException("文章不存在");
        }
        articleRepository.deleteById(articleId);
    }

    public void incrementViewCount(Long articleId) {
        articleRepository.incrementViewCount(articleId);
    }

    // 修复：获取文章统计数据
    public Map<String, Object> getArticleStats(Long articleId) {
        try {
            long likeCount = likeRepository.countByArticle_Id(articleId);
            long favCount = favoriteRepository.countByArticle_Id(articleId);
            long commentCount = commentRepository.countByArticleId(articleId);

            return Map.of(
                    "likeCount", likeCount,
                    "favCount", favCount,
                    "commentCount", commentCount
            );
        } catch (Exception e) {
            return Map.of(
                    "likeCount", 0L,
                    "favCount", 0L,
                    "commentCount", 0L
            );
        }
    }

    // 修复：获取用户交互状态
    public Map<String, Object> getUserInteractionStatus(Long articleId, Long userId) {
        try {
            boolean liked = likeRepository.findByUser_IdAndArticle_Id(userId, articleId).isPresent();
            boolean favorited = favoriteRepository.findByUser_IdAndArticle_Id(userId, articleId).isPresent();
            long likeCount = likeRepository.countByArticle_Id(articleId);
            long favCount = favoriteRepository.countByArticle_Id(articleId);
            long commentCount = commentRepository.countByArticleId(articleId);

            return Map.of(
                    "liked", liked,
                    "favorited", favorited,
                    "likeCount", likeCount,
                    "favCount", favCount,
                    "commentCount", commentCount
            );
        } catch (Exception e) {
            return Map.of(
                    "liked", false,
                    "favorited", false,
                    "likeCount", 0L,
                    "favCount", 0L,
                    "commentCount", 0L
            );
        }
    }

    // 在 ArticleService.java 中添加
    public Page<Article> findByKeywordAndAuthor(String keyword, Long authorId, String sortBy, Pageable pageable) {
        if (authorId != null) {
            // 如果指定了作者ID，则只查询该作者的文章
            return articleRepository.findByAuthorIdAndPublishedTrue(authorId, pageable);
        }

        // 否则使用原来的查询逻辑
        return findByKeyword(keyword, sortBy, pageable);
    }
}
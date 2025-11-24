package sub.boke.web;

import lombok.Data;
import sub.boke.domain.Article;

import java.time.Instant;

@Data
public class ArticleResponse {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String coverImageUrl;
    private String author;
    private Instant createdAt;
    private Instant updatedAt;
    private Long likeCount;
    private Long favCount;

    public static ArticleResponse fromEntity(Article article) {
        ArticleResponse response = new ArticleResponse();
        response.setId(article.getId());
        response.setTitle(article.getTitle());
        response.setSummary(article.getSummary());
        response.setContent(article.getContent());
        response.setCoverImageUrl(article.getCoverImageUrl());
        response.setAuthor(article.getAuthor().getUsername()); // 或者 displayName，根据需求
        response.setCreatedAt(article.getCreatedAt());
        response.setUpdatedAt(article.getUpdatedAt());
        // 注意：likeCount和favCount需要从服务层计算并设置
        return response;
    }
}
package sub.boke.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sub.boke.domain.Article;
import sub.boke.domain.User;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    // 新增：按时间正序分页查询所有文章（最旧的在上面）
    Page<Article> findAllByOrderByCreatedAtAsc(Pageable pageable);

    // 按时间倒序分页查询所有文章
    Page<Article> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 搜索文章并按时间倒序排序（最新的在上面）
    @Query("SELECT a FROM Article a WHERE a.title LIKE %:keyword% OR a.summary LIKE %:keyword% ORDER BY a.createdAt DESC")
    Page<Article> findByTitleContainingOrSummaryContainingOrderByCreatedAtDesc(@Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(value = "Article.withAuthor", type = EntityGraph.EntityGraphType.FETCH)
    Page<Article> findAll(Pageable pageable);

    // 统计点赞数
    @Query("SELECT COUNT(al) FROM ArticleLike al WHERE al.article.id = :articleId")
    Long countLikesByArticleId(@Param("articleId") Long articleId);

    // 统计收藏数
    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.article.id = :articleId")
    Long countFavoritesByArticleId(@Param("articleId") Long articleId);

    // 新增：增加浏览量
    @Modifying
    @Query("UPDATE Article a SET a.viewCount = a.viewCount + 1 WHERE a.id = :articleId")
    void incrementViewCount(@Param("articleId") Long articleId);

    // 添加这个方法确保能正确加载author
    // 根据ID查找文章并加载作者信息（用于文章详情）
    //用于表示一个值可能存在也可能不存在。使用 Optional 可以避免显式的 null 检查，从而减少 NullPointerException。
    @EntityGraph(attributePaths = {"author"})
    Optional<Article> findWithAuthorById(Long id);


    @EntityGraph(attributePaths = {"author"})
    Page<Article> findByTitleContainingOrSummaryContainingOrderByCreatedAtDesc(String title, String summary, Pageable pageable);


    // 新增：搜索文章并按时间正序排序（最旧的在上面）
    @Query("SELECT a FROM Article a WHERE a.title LIKE %:keyword% OR a.summary LIKE %:keyword% ORDER BY a.createdAt ASC")
    Page<Article> findByTitleContainingOrSummaryContainingOrderByCreatedAtAsc(@Param("keyword") String keyword, Pageable pageable);

    // 新增：草稿相关查询
    // 查询用户的草稿（按更新时间倒序）
    Page<Article> findByAuthorAndPublishedFalseOrderByUpdatedAtDesc(User author, Pageable pageable);

    // 查询已发布的文章（用于首页）
    Page<Article> findByPublishedTrue(Pageable pageable);
    Page<Article> findByPublishedTrueOrderByCreatedAtDesc(Pageable pageable);
    Page<Article> findByPublishedTrueOrderByCreatedAtAsc(Pageable pageable);

    // 搜索已发布的文章
    @Query("SELECT a FROM Article a WHERE (a.title LIKE %:keyword% OR a.summary LIKE %:keyword%) AND a.published = true ORDER BY a.createdAt DESC")
    Page<Article> findByTitleContainingOrSummaryContainingAndPublishedTrueOrderByCreatedAtDesc(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT a FROM Article a WHERE (a.title LIKE %:keyword% OR a.summary LIKE %:keyword%) AND a.published = true ORDER BY a.createdAt ASC")
    Page<Article> findByTitleContainingOrSummaryContainingAndPublishedTrueOrderByCreatedAtAsc(@Param("keyword") String keyword, Pageable pageable);



    Page<Article> findByAuthorIdAndPublishedTrue(Long authorId, Pageable pageable);
    Page<Article> findByAuthorId(Long authorId, Pageable pageable); // 如果需要包含草稿


    long countByAuthorIdAndPublishedTrue(Long authorId);

    // 如果需要实现点赞数和阅读数统计，添加这些方法：
    @Query("SELECT COALESCE(SUM(a.viewCount), 0) FROM Article a WHERE a.author.id = :authorId AND a.published = true")
    long sumViewCountByAuthorId(@Param("authorId") Long authorId);

    @Query("SELECT COALESCE(COUNT(al), 0) FROM ArticleLike al WHERE al.article.author.id = :authorId AND al.article.published = true")
    long sumLikeCountByAuthorId(@Param("authorId") Long authorId);
    // 以下方法暂时未使用，注释掉
    /*
    // 为了支持不分关键词的查询，我们还需要一个查询所有并加载author的方法
    @Query("SELECT a FROM Article a LEFT JOIN FETCH a.author")
    Page<Article> findAllWithAuthor(Pageable pageable);

    Page<Article> findByTitleContainingOrSummaryContaining(String title, String summary, Pageable pageable);

    @EntityGraph(value = "Article.withAuthor", type = EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT a FROM Article a WHERE a.title LIKE %:keyword% OR a.summary LIKE %:keyword%")
    Page<Article> findByTitleContainingOrSummaryContaining(@Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(value = "Article.withAuthor", type = EntityGraph.EntityGraphType.FETCH)
    Page<Article> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"author"})
    Page<Article> findByTitleContainingOrSummaryContainingOrderByCreatedAtDesc(String title, String summary, Pageable pageable);
    */
}

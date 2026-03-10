package sub.boke.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sub.boke.domain.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByArticle_IdOrderByCreatedAtAsc(Long articleId, Pageable pageable);
    // 统计文章的评论数
    // 确保这个方法正确实现
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.article.id = :articleId")
    long countByArticleId(@Param("articleId") Long articleId);

}

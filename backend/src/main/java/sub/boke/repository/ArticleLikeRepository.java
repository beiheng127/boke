package sub.boke.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sub.boke.domain.ArticleLike;

import java.util.Optional;

public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {
    Optional<ArticleLike> findByUser_IdAndArticle_Id(Long userId, Long articleId);

    long countByArticle_Id(Long articleId);

}

package sub.boke.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sub.boke.domain.Favorite;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUser_IdAndArticle_Id(Long userId, Long articleId);

    Page<Favorite> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByArticle_Id(Long articleId);
}

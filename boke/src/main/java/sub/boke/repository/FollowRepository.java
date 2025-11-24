package sub.boke.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sub.boke.domain.Follow;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    Page<Follow> findByFollowerId(Long followerId, Pageable pageable);

    Page<Follow> findByFollowingId(Long followingId, Pageable pageable);

    long countByFollowerId(Long followerId);

    long countByFollowingId(Long followingId);

    @Query("SELECT f FROM Follow f JOIN FETCH f.following WHERE f.follower.id = :followerId")
    Page<Follow> findFollowingWithUser(@Param("followerId") Long followerId, Pageable pageable);

    @Query("SELECT f FROM Follow f JOIN FETCH f.follower WHERE f.following.id = :followingId")
    Page<Follow> findFollowersWithUser(@Param("followingId") Long followingId, Pageable pageable);
}
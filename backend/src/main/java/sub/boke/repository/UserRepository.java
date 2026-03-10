package sub.boke.repository;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sub.boke.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    // 修复搜索方法 - 使用 @Query 注解
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.displayName LIKE %:keyword%")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    // 新增：根据邮箱查找用户
    Optional<User> findByEmail(String email);

}

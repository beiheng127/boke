package sub.boke.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sub.boke.domain.Conversation;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // 查找两个用户之间的会话
    @Query("SELECT c FROM Conversation c WHERE " +
            "(c.user1.id = :user1Id AND c.user2.id = :user2Id) OR " +
            "(c.user1.id = :user2Id AND c.user2.id = :user1Id)")
    Optional<Conversation> findByUserPair(@Param("user1Id") Long user1Id,
                                          @Param("user2Id") Long user2Id);

    // 查找用户的所有会话，按最后消息时间排序
    @Query("SELECT c FROM Conversation c WHERE " +
            "c.user1.id = :userId OR c.user2.id = :userId " +
            "ORDER BY c.lastMessageAt DESC NULLS LAST")
    Page<Conversation> findByUserId(@Param("userId") Long userId, Pageable pageable);

    // 获取用户的未读消息总数
    @Query("SELECT COALESCE(SUM(CASE WHEN c.user1.id = :userId THEN c.unreadCountUser1 ELSE c.unreadCountUser2 END), 0) " +
            "FROM Conversation c WHERE c.user1.id = :userId OR c.user2.id = :userId")
    Integer sumUnreadCountByUserId(@Param("userId") Long userId);
}
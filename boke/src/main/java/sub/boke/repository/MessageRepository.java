package sub.boke.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sub.boke.domain.Message;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // 查找两个用户之间的消息，按时间倒序
    @Query("SELECT m FROM Message m WHERE " +
            "((m.sender.id = :user1Id AND m.receiver.id = :user2Id) OR " +
            "(m.sender.id = :user2Id AND m.receiver.id = :user1Id)) " +
            "AND m.isDeleted = false " +
            "ORDER BY m.createdAt DESC")
    Page<Message> findConversation(@Param("user1Id") Long user1Id,
                                   @Param("user2Id") Long user2Id,
                                   Pageable pageable);

    // 查找用户收到的未读消息数量
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :userId AND m.isRead = false AND m.isDeleted = false")
    long countUnreadMessages(@Param("userId") Long userId);

    // 查找与特定用户的未读消息数量
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :receiverId AND m.sender.id = :senderId AND m.isRead = false AND m.isDeleted = false")
    long countUnreadMessagesFromUser(@Param("senderId") Long senderId,
                                     @Param("receiverId") Long receiverId);

    // 标记来自某个发送者的所有消息为已读
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = :readAt WHERE m.sender.id = :senderId AND m.receiver.id = :receiverId AND m.isRead = false")
    void markMessagesAsRead(@Param("senderId") Long senderId,
                            @Param("receiverId") Long receiverId,
                            @Param("readAt") Instant readAt);

    // 查找用户最后收到的消息
    @Query("SELECT m FROM Message m WHERE m.receiver.id = :userId AND m.isDeleted = false ORDER BY m.createdAt DESC LIMIT 1")
    Optional<Message> findLastReceivedMessage(@Param("userId") Long userId);
}
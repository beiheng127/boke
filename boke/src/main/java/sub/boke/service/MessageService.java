package sub.boke.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sub.boke.domain.Conversation;
import sub.boke.domain.Message;
import sub.boke.domain.User;
import sub.boke.repository.ConversationRepository;
import sub.boke.repository.MessageRepository;
import sub.boke.repository.UserRepository;

import java.time.Instant;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public Message sendMessage(Long senderId, Long receiverId, String content, Message.MessageType type) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("发送者不存在"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("接收者不存在"));

        // 创建消息
        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .type(type)
                .isRead(false)
                .isDeleted(false)
                .createdAt(Instant.now())
                .build();

        Message savedMessage = messageRepository.save(message);

        // 更新或创建会话
        updateOrCreateConversation(senderId, receiverId, savedMessage);

        return savedMessage;
    }

    public Message sendFileMessage(Long senderId, Long receiverId, String content,
                                   String fileUrl, String fileName, Long fileSize) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("发送者不存在"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("接收者不存在"));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .type(Message.MessageType.FILE)
                .fileUrl(fileUrl)
                .fileName(fileName)
                .fileSize(fileSize)
                .isRead(false)
                .isDeleted(false)
                .createdAt(Instant.now())
                .build();

        Message savedMessage = messageRepository.save(message);
        updateOrCreateConversation(senderId, receiverId, savedMessage);

        return savedMessage;
    }

    private void updateOrCreateConversation(Long user1Id, Long user2Id, Message lastMessage) {
        // 确保 user1Id < user2Id 以保持一致性
        Long minId = Math.min(user1Id, user2Id);
        Long maxId = Math.max(user1Id, user2Id);

        Optional<Conversation> conversationOpt = conversationRepository.findByUserPair(minId, maxId);

        Conversation conversation;
        if (conversationOpt.isPresent()) {
            conversation = conversationOpt.get();
        } else {
            User user1 = userRepository.findById(minId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            User user2 = userRepository.findById(maxId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            conversation = Conversation.builder()
                    .user1(user1)
                    .user2(user2)
                    .unreadCountUser1(0)
                    .unreadCountUser2(0)
                    .createdAt(Instant.now())
                    .build();
        }

        // 更新会话信息
        conversation.setLastMessage(lastMessage);
        conversation.setLastMessageContent(lastMessage.getContent());
        conversation.setLastMessageAt(lastMessage.getCreatedAt());

        // 更新未读计数
        if (user1Id.equals(minId)) {
            // user1 是发送者，增加 user2 的未读计数
            conversation.setUnreadCountUser2(conversation.getUnreadCountUser2() + 1);
        } else {
            // user2 是发送者，增加 user1 的未读计数
            conversation.setUnreadCountUser1(conversation.getUnreadCountUser1() + 1);
        }

        conversationRepository.save(conversation);
    }

    public Page<Message> getConversation(Long user1Id, Long user2Id, Pageable pageable) {
        return messageRepository.findConversation(user1Id, user2Id, pageable);
    }

    public Page<Conversation> getConversations(Long userId, Pageable pageable) {
        return conversationRepository.findByUserId(userId, pageable);
    }

    public void markConversationAsRead(Long senderId, Long receiverId) {
        messageRepository.markMessagesAsRead(senderId, receiverId, Instant.now());

        // 更新会话中的未读计数
        Long minId = Math.min(senderId, receiverId);
        Long maxId = Math.max(senderId, receiverId);

        conversationRepository.findByUserPair(minId, maxId).ifPresent(conversation -> {
            if (receiverId.equals(minId)) {
                conversation.setUnreadCountUser1(0);
            } else {
                conversation.setUnreadCountUser2(0);
            }
            conversationRepository.save(conversation);
        });
    }

    public long getUnreadCount(Long userId) {
        return messageRepository.countUnreadMessages(userId);
    }

    public boolean recallMessage(Long messageId, Long userId) {
        return messageRepository.findById(messageId)
                .map(message -> {
                    // 只有发送者可以撤回消息，且只能在2分钟内撤回
                    if (!message.getSender().getId().equals(userId)) {
                        throw new RuntimeException("只能撤回自己发送的消息");
                    }

                    if (message.getCreatedAt().isBefore(Instant.now().minusSeconds(120))) {
                        throw new RuntimeException("消息发送超过2分钟，无法撤回");
                    }

                    message.setIsDeleted(true);
                    messageRepository.save(message);
                    return true;
                })
                .orElse(false);
    }
}
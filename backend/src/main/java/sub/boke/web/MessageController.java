package sub.boke.web;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sub.boke.domain.Conversation;
import sub.boke.domain.Message;
import sub.boke.domain.User;
import sub.boke.service.FileStorageService;
import sub.boke.service.MessageService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final FileStorageService fileStorageService;

    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestBody SendMessageRequest request,
                                         @AuthenticationPrincipal User sender) {
        try {
            Message message = messageService.sendMessage(
                    sender.getId(),
                    request.getReceiverId(),
                    request.getContent(),
                    Message.MessageType.TEXT
            );
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/file")
    public ResponseEntity<?> sendFileMessage(
            @RequestParam("receiverId") Long receiverId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User sender) {

        try {
            // 文件验证
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "文件不能为空"));
            }

            // 保存文件
            String savePath = fileStorageService.saveFile(file, "messages");
            String fileUrl = "/uploads" + savePath;

            Message message = messageService.sendFileMessage(
                    sender.getId(),
                    receiverId,
                    content != null ? content : file.getOriginalFilename(),
                    fileUrl,
                    file.getOriginalFilename(),
                    file.getSize()
            );

            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "发送文件失败: " + e.getMessage()));
        }
    }

    @GetMapping("/with/{userId}")
    public ResponseEntity<?> getConversation(@PathVariable Long userId,
                                             @AuthenticationPrincipal User currentUser,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Message> messages = messageService.getConversation(currentUser.getId(), userId, pageable);
            return ResponseEntity.ok(PageResponse.of(messages));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取聊天记录失败: " + e.getMessage()));
        }
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations(@AuthenticationPrincipal User currentUser,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Conversation> conversations = messageService.getConversations(currentUser.getId(), pageable);
            return ResponseEntity.ok(PageResponse.of(conversations));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取会话列表失败: " + e.getMessage()));
        }
    }

    @PutMapping("/read")
    public ResponseEntity<?> markAsRead(@RequestBody MarkAsReadRequest request,
                                        @AuthenticationPrincipal User currentUser) {
        try {
            messageService.markConversationAsRead(request.getSenderId(), currentUser.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "标记已读失败: " + e.getMessage()));
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(@AuthenticationPrincipal User currentUser) {
        try {
            long count = messageService.getUnreadCount(currentUser.getId());
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取未读消息数失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<?> recallMessage(@PathVariable Long messageId,
                                           @AuthenticationPrincipal User currentUser) {
        try {
            boolean success = messageService.recallMessage(messageId, currentUser.getId());
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 请求类
    @Data
    public static class SendMessageRequest {
        private Long receiverId;
        private String content;
    }

    @Data
    public static class MarkAsReadRequest {
        private Long senderId;
    }
}
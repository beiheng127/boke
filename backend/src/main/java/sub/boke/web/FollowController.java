package sub.boke.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sub.boke.domain.Follow;
import sub.boke.domain.User;
import sub.boke.service.FollowService;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/follows")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<?> followUser(@PathVariable Long userId, @AuthenticationPrincipal User user) {
        try {
            followService.followUser(user.getId(), userId, Instant.now(),Instant.now());
            return ResponseEntity.ok(Map.of("success", true, "message", "关注成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> unfollowUser(@PathVariable Long userId, @AuthenticationPrincipal User user) {
        try {
            followService.unfollowUser(user.getId(), userId);
            return ResponseEntity.ok(Map.of("success", true, "message", "取消关注成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{userId}/is-following")
    public ResponseEntity<?> isFollowing(@PathVariable Long userId, @AuthenticationPrincipal User user) {
        try {
            boolean isFollowing = followService.isFollowing(user.getId(), userId);
            return ResponseEntity.ok(Map.of("isFollowing", isFollowing));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<?> getFollowingList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Follow> following = followService.getFollowingList(userId, pageable);

            Page<Map<String, Object>> result = following.map(follow -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", follow.getId());
                map.put("createdAt", follow.getCreatedAt());
                map.put("user", convertUserToMap(follow.getFollowing()));
                return map;
            });

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<?> getFollowersList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Follow> followers = followService.getFollowersList(userId, pageable);

            Page<Map<String, Object>> result = followers.map(follow -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", follow.getId());
                map.put("createdAt", follow.getCreatedAt());
                map.put("user", convertUserToMap(follow.getFollower()));
                return map;
            });

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{userId}/stats")
    public ResponseEntity<?> getFollowStats(@PathVariable Long userId) {
        try {
            FollowService.FollowStats stats = followService.getFollowStats(userId);
            return ResponseEntity.ok(Map.of(
                    "followingCount", stats.getFollowingCount(),
                    "followersCount", stats.getFollowersCount()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("displayName", user.getDisplayName());
        userMap.put("avatarUrl", user.getAvatarUrl());
        userMap.put("signature", user.getSignature());
        userMap.put("role", user.getRole());
        return userMap;
    }
}
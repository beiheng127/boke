package sub.boke.service;

import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sub.boke.domain.Follow;
import sub.boke.domain.User;
import sub.boke.repository.FollowRepository;
import sub.boke.repository.UserRepository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowService(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    public void followUser(Long followerId, Long followingId, Instant createdAt, Instant updatedAt) {
        if (followerId.equals(followingId)) {
            throw new RuntimeException("不能关注自己");
        }

        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new RuntimeException("已经关注该用户");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("关注者不存在"));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("被关注者不存在"));

        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        followRepository.save(follow);
    }

    public void unfollowUser(Long followerId, Long followingId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new RuntimeException("未关注该用户"));
        followRepository.delete(follow);
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    public Page<Follow> getFollowingList(Long userId, Pageable pageable) {
        return followRepository.findFollowingWithUser(userId, pageable);
    }

    public Page<Follow> getFollowersList(Long userId, Pageable pageable) {
        return followRepository.findFollowersWithUser(userId, pageable);
    }

    public FollowStats getFollowStats(Long userId) {
        Long followingCount = followRepository.countByFollowerId(userId);
        Long followersCount = followRepository.countByFollowingId(userId);
        return new FollowStats(followingCount, followersCount);
    }

    @Data
    public static class FollowStats {
        private final Long followingCount;
        private final Long followersCount;
    }
}
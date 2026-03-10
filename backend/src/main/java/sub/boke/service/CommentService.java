// CommentService.java - 完善评论服务
package sub.boke.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sub.boke.domain.Comment;
import sub.boke.domain.User;
import sub.boke.domain.Article;
import sub.boke.repository.CommentRepository;
import sub.boke.repository.ArticleRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;


    // 统计文章的评论总数
    public long countCommentsByArticleId(Long articleId) {
        return commentRepository.countByArticleId(articleId);
    }

}
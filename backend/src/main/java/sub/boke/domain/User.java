package sub.boke.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
//如果User类被其他实体引用，并且是懒加载，那么当序列化时，Hibernate会生成代理对象，而代理对象包含了一些Jackson无法序列化的属性。
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "fieldHandler"})
public class User {

    public enum Role {
        BLOGGER, VIEWER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

//    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "home_background_url", length = 500)
    private String homeBackgroundUrl;

    // 获取头像URL，如果为空则返回默认头像
    public String getAvatarUrl() {
        return avatarUrl != null && !avatarUrl.trim().isEmpty()
                ? avatarUrl
                : "/uploads/images/default/touxiang.jpg";
    }

    @Column(name = "signature", length = 200)
    private String signature;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;


}
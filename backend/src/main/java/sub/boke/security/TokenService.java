// TokenService.java - 使用JWT持久化
package sub.boke.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sub.boke.config.JwtConfig;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class TokenService {

    @Autowired
    private JwtConfig jwtConfig;

    private SecretKey getSigningKey() {
        // 确保密钥长度足够（至少256位）
        String secret = jwtConfig.getSecret();
        if (secret.length() < 32) {
            // 如果密钥太短，进行填充
            StringBuilder sb = new StringBuilder(secret);
            while (sb.length() < 32) {
                sb.append("0");
            }
            secret = sb.toString();
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String issueToken(Long userId) {
        try {
            System.out.println("🔍 TokenService - 生成JWT Token，用户ID: " + userId);

            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());

            String token = Jwts.builder()
                    .setSubject(userId.toString())
                    .claim("userId", userId)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();

            System.out.println("🔍 TokenService - 生成的JWT Token: " + token);
            System.out.println("🔍 TokenService - Token长度: " + token.length());
            System.out.println("🔍 TokenService - 过期时间: " + expiryDate);

            return token;
        } catch (Exception e) {
            System.out.println("❌ TokenService - 生成JWT Token失败: " + e.getMessage());
            throw new RuntimeException("生成Token失败", e);
        }
    }

    public Long verifyToken(String token) {
        try {
            System.out.println("🔍 TokenService - 验证JWT Token: " + (token != null ? token.substring(0, Math.min(10, token.length())) + "..." : "null"));

            if (token == null || token.trim().isEmpty()) {
                System.out.println("❌ TokenService - Token为空");
                return null;
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Long userId = claims.get("userId", Long.class);
            System.out.println("🔍 TokenService - JWT验证成功，用户ID: " + userId);

            return userId;
        } catch (ExpiredJwtException e) {
            System.out.println("❌ TokenService - JWT Token已过期: " + e.getMessage());
            return null;
        } catch (MalformedJwtException e) {
            System.out.println("❌ TokenService - JWT Token格式错误: " + e.getMessage());
            return null;
        } catch (SignatureException e) {
            System.out.println("❌ TokenService - JWT签名验证失败: " + e.getMessage());
            return null;
        } catch (JwtException e) {
            System.out.println("❌ TokenService - JWT验证异常: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("❌ TokenService - 验证Token时发生未知异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // 为了兼容性，保留resolveUserId方法（与verifyToken相同）
    public Long resolveUserId(String token) {
        return verifyToken(token);
    }

    // JWT是无状态的，不需要显式撤销，但可以维护一个黑名单（可选）
    public void revoke(String token) {
        // 对于JWT，由于无状态，通常不实现撤销
        // 如果需要撤销功能，可以维护一个黑名单在Redis或数据库中
        System.out.println("⚠️  TokenService - JWT Token撤销请求（JWT无状态，实际不执行）: " + token);
    }

    // 添加调试方法
    public String getDebugInfo() {
        return "JWT Token Service - 使用HS256算法，密钥长度: " +
                (jwtConfig.getSecret() != null ? jwtConfig.getSecret().length() : 0) +
                ", 过期时间: " + jwtConfig.getExpiration() + "ms";
    }

    // 解析Token信息（用于调试）
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            System.out.println("❌ TokenService - 解析Token失败: " + e.getMessage());
            return null;
        }
    }
}
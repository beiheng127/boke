// DebugController.java
package sub.boke.web;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sub.boke.security.TokenService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController2 {

    @Autowired
    private TokenService tokenService;

    @GetMapping("/token")
    public ResponseEntity<?> debugToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        System.out.println("=== 🔍 JWT Token调试信息 ===");

        Map<String, Object> result = new HashMap<>();
        result.put("serviceType", "JWT Token Service");
        result.put("debugInfo", tokenService.getDebugInfo());

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            result.put("token", token.substring(0, Math.min(10, token.length())) + "...");
            result.put("tokenLength", token.length());

            // 验证Token
            Long userId = tokenService.verifyToken(token);
            result.put("userId", userId);
            result.put("isValid", userId != null);

            // 解析Token详细信息
            Claims claims = tokenService.parseToken(token);
            if (claims != null) {
                Map<String, Object> tokenInfo = new HashMap<>();
                tokenInfo.put("subject", claims.getSubject());
                tokenInfo.put("issuedAt", claims.getIssuedAt());
                tokenInfo.put("expiration", claims.getExpiration());
                tokenInfo.put("userId", claims.get("userId"));
                result.put("tokenDetails", tokenInfo);
            }
        } else {
            result.put("error", "Authorization头格式不正确或不存在");
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/jwt-test")
    public ResponseEntity<?> testJwt() {
        System.out.println("=== 🔍 JWT功能测试 ===");

        Map<String, Object> result = new HashMap<>();

        try {
            // 测试生成Token
            Long testUserId = 1L;
            String token = tokenService.issueToken(testUserId);
            result.put("generatedToken", token);

            // 测试验证Token
            Long verifiedUserId = tokenService.verifyToken(token);
            result.put("verifiedUserId", verifiedUserId);
            result.put("verificationSuccess", testUserId.equals(verifiedUserId));

            // 测试解析Token
            Claims claims = tokenService.parseToken(token);
            if (claims != null) {
                result.put("claims", Map.of(
                        "subject", claims.getSubject(),
                        "userId", claims.get("userId"),
                        "issuedAt", claims.getIssuedAt(),
                        "expiration", claims.getExpiration()
                ));
            }

            result.put("testResult", "SUCCESS");

        } catch (Exception e) {
            result.put("testResult", "FAILED");
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}
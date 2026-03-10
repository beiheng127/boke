package sub.boke.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sub.boke.domain.User;
import sub.boke.repository.UserRepository;

import java.io.IOException;
import java.util.Collections;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    // 定义不需要认证的路径
    private static final String[] EXCLUDED_PATHS = {
            "/api/auth/",  // 认证接口
            "/uploads/",   // 静态资源
            "/error",      // 错误页面
            "/favicon.ico" // 网站图标
    };

    public AuthFilter(TokenService tokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // 检查是否是需要排除的路径
        for (String excludedPath : EXCLUDED_PATHS) {
            if (path.startsWith(excludedPath)) {
                System.out.println("✅ AuthFilter - 跳过认证检查: " + path);
                return true;
            }
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        System.out.println("🔍 AuthFilter - 请求: " + method + " " + requestURI);
        System.out.println("🔍 AuthFilter - Authorization头: " + (authHeader != null ? "存在" : "不存在"));

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Long userId = tokenService.verifyToken(token);
                System.out.println("🔍 AuthFilter - JWT Token验证结果, 用户ID: " + userId);

                if (userId != null) {
                    User user = userRepository.findById(userId).orElse(null);
                    if (user != null) {
                        System.out.println("🔍 AuthFilter - 用户验证成功: " + user.getUsername() + ", 角色: " + user.getRole());

                        // 创建认证对象
                        String authority = "ROLE_" + user.getRole().name();
                        System.out.println("🔍 AuthFilter - 设置权限: " + authority);

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(user, null,
                                        Collections.singleton(new SimpleGrantedAuthority(authority)));
                        SecurityContextHolder.getContext().setAuthentication(auth);

                        System.out.println("✅ AuthFilter - JWT认证成功，用户已设置到SecurityContext");
                    } else {
                        System.out.println("❌ AuthFilter - 用户不存在，ID: " + userId);
                    }
                } else {
                    System.out.println("❌ AuthFilter - JWT Token无效或过期");
                }
            } catch (Exception e) {
                System.out.println("❌ AuthFilter - JWT Token验证异常: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ AuthFilter - 无认证头或格式不正确");
        }

        // 检查认证后的上下文
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            System.out.println("✅ AuthFilter - 当前认证用户: " + authentication.getName());
            System.out.println("✅ AuthFilter - 当前认证权限: " + authentication.getAuthorities());
        } else {
            System.out.println("❌ AuthFilter - 无有效认证");
        }

        filterChain.doFilter(request, response);
    }
}


//package sub.boke.security;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import sub.boke.domain.User;
//import sub.boke.repository.UserRepository;
//
//import java.io.IOException;
//import java.util.Collections;
//
//@Component
//public class AuthFilter extends OncePerRequestFilter {
//
//    private final TokenService tokenService;
//    private final UserRepository userRepository;
//
//    // 定义不需要认证的路径
//    private static final String[] EXCLUDED_PATHS = {
//            "/api/auth/",  // 认证接口
//            "/uploads/",   // 静态资源
//            "/error",      // 错误页面
//            "/favicon.ico" // 网站图标
//    };
//
//    public AuthFilter(TokenService tokenService, UserRepository userRepository) {
//        this.tokenService = tokenService;
//        this.userRepository = userRepository;
//    }
//
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String path = request.getRequestURI();
//
//        // 检查是否是需要排除的路径
//        for (String excludedPath : EXCLUDED_PATHS) {
//            if (path.startsWith(excludedPath)) {
//                System.out.println("✅ AuthFilter - 跳过认证检查: " + path);
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        String authHeader = request.getHeader("Authorization");
//        String requestURI = request.getRequestURI();
//        String method = request.getMethod();
//
//        System.out.println("🔍 AuthFilter - 请求: " + method + " " + requestURI);
//        System.out.println("🔍 AuthFilter - Authorization头: " + (authHeader != null ? "存在" : "不存在"));
//
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            String token = authHeader.substring(7);
//            try {
//                Long userId = tokenService.verifyToken(token);
//                System.out.println("🔍 AuthFilter - Token验证结果, 用户ID: " + userId);
//
//                if (userId != null) {
//                    User user = userRepository.findById(userId).orElse(null);
//                    if (user != null) {
//                        System.out.println("🔍 AuthFilter - 用户验证成功: " + user.getUsername() + ", 角色: " + user.getRole());
//
//                        // 创建认证对象
//                        String authority = "ROLE_" + user.getRole().name();
//                        System.out.println("🔍 AuthFilter - 设置权限: " + authority);
//
//                        UsernamePasswordAuthenticationToken auth =
//                                new UsernamePasswordAuthenticationToken(user, null,
//                                        Collections.singleton(new SimpleGrantedAuthority(authority)));
//                        SecurityContextHolder.getContext().setAuthentication(auth);
//
//                        System.out.println("🔍 AuthFilter - 认证已设置到SecurityContext");
//                    } else {
//                        System.out.println("❌ AuthFilter - 用户不存在，ID: " + userId);
//                    }
//                } else {
//                    System.out.println("❌ AuthFilter - Token无效或过期");
//                }
//            } catch (Exception e) {
//                System.out.println("❌ AuthFilter - Token验证异常: " + e.getMessage());
//            }
//        } else {
//            System.out.println("⚠️ AuthFilter - 无认证头或格式不正确");
//        }
//
//        // 检查认证后的上下文
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication != null && authentication.isAuthenticated()) {
//            System.out.println("✅ AuthFilter - 当前认证用户: " + authentication.getName());
//            System.out.println("✅ AuthFilter - 当前认证权限: " + authentication.getAuthorities());
//        } else {
//            System.out.println("❌ AuthFilter - 无有效认证");
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//
//
//    //    @Override
////    protected void doFilterInternal(HttpServletRequest request,
////                                    HttpServletResponse response,
////                                    FilterChain filterChain) throws ServletException, IOException {
////
////        String authHeader = request.getHeader("Authorization");
////
////        if (authHeader != null && authHeader.startsWith("Bearer ")) {
////            String token = authHeader.substring(7);
////            try {
////                Long userId = tokenService.verifyToken(token);
////                if (userId != null) {
////                    User user = userRepository.findById(userId).orElse(null);
////                    if (user != null) {
////                        // 创建认证对象
////                        // 在 AuthFilter.java 中修改角色处理
////                        UsernamePasswordAuthenticationToken auth =
////                                new UsernamePasswordAuthenticationToken(user, null,
////                                        Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
////                        SecurityContextHolder.getContext().setAuthentication(auth);
////                    }
////                }
////            } catch (Exception e) {
////                // Token验证失败，继续匿名访问
////                logger.debug("Token verification failed: " + e.getMessage());
////            }
////        }
////
////        filterChain.doFilter(request, response);
////    }
//}
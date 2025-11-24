package sub.boke.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthFilter authFilter;

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    public SecurityConfig(AuthFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(registry -> registry
                        .requestMatchers("/h2/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/articles/**").permitAll()
                        // 临时修改：允许所有认证用户发布文章进行测试
                        .requestMatchers(HttpMethod.POST, "/api/articles/**").hasRole("BLOGGER")
                        .requestMatchers(HttpMethod.PUT, "/api/articles/**").hasRole("BLOGGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/articles/**").hasRole("BLOGGER")
                        .requestMatchers("/api/auth/**", "/api/ai/**").permitAll()
                        .requestMatchers("/api/comments/**").authenticated()
                        .requestMatchers("/api/interact/**").authenticated()
                        .requestMatchers("/api/users/**").authenticated()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/uploads/images/default/**").permitAll()
                        .requestMatchers("/uploads/private/**").authenticated()
                        .requestMatchers("/test/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("BLOGGER")
                        .requestMatchers("/api/debug/**").permitAll()
                        .requestMatchers("/api/follows/**").authenticated()
                        .requestMatchers("/api/messages/**").authenticated()
                        .requestMatchers("/api/auth/send-code").permitAll()
                        .requestMatchers("/api/auth/send-reset-code").permitAll()
                        .requestMatchers("/api/auth/reset-password").permitAll()
                        .anyRequest().denyAll())
                .headers(h -> h.frameOptions(frame -> frame.disable()));
        http.addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


//    //临时放宽权限
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//                .cors(Customizer.withDefaults())
//                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(registry -> registry
//                        .requestMatchers("/h2/**").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/api/articles/**").permitAll()
//                        // 临时修改：允许所有认证用户发布文章进行测试
//                        .requestMatchers(HttpMethod.POST, "/api/articles/**").authenticated()
//                        .requestMatchers(HttpMethod.PUT, "/api/articles/**").authenticated()
//                        .requestMatchers(HttpMethod.DELETE, "/api/articles/**").authenticated()
//                        .requestMatchers("/api/auth/**", "/api/ai/**").permitAll()
//                        .requestMatchers("/api/comments/**").authenticated()
//                        .requestMatchers("/api/interact/**").authenticated()
//                        .requestMatchers("/api/users/**").authenticated()
//                        .requestMatchers("/uploads/**").permitAll()
//                        .requestMatchers("/uploads/images/default/**").permitAll()  // 默认头像公开
//                        .requestMatchers("/uploads/private/**").authenticated() // 私有文件夹需要认证
//                        .requestMatchers("/test/**").permitAll()
//                        .requestMatchers("/api/admin/**").hasRole("BLOGGER")
//                        .requestMatchers("/api/debug/**").permitAll()
//                        .anyRequest().denyAll())
//                .headers(h -> h.frameOptions(frame -> frame.disable()));
//        http.addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
//        return http.build();
//    }

    //全部放宽
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//                .cors(Customizer.withDefaults())
//                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(registry -> registry
//                        .anyRequest().permitAll() // 允许所有请求，包括静态资源
//                );
//        http.addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
//        return http.build();
//    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

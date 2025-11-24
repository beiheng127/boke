package sub.boke.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sub.boke.domain.User;
import sub.boke.repository.UserRepository;
import sub.boke.service.UserService;

@Configuration
public class AdminInitializer {

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.email:admin@example.com}")
    private String adminEmail;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository, UserService userService) {
        return args -> {
            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                userService.register(adminUsername, adminEmail, adminPassword, User.Role.BLOGGER, "博主");
            }
        };
    }
}
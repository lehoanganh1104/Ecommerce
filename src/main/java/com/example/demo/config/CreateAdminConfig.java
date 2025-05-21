package com.example.demo.config;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CreateAdminConfig {
    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository){
        return args -> {
            boolean adminExists = userRepository.existsByUsernameIgnoreCaseAndDeletedFalse("admin");
            if (!adminExists) {
                User user = new User();
                user.setUsername("admin");
                user.setPassword(passwordEncoder.encode("admin"));
                user.setRole(User.Role.ADMIN);
                userRepository.save(user);
                log.info("Default admin user created with username 'admin' and password 'admin'");
            } else {
                log.info("Admin user already exists");
            }
        };
    }
}

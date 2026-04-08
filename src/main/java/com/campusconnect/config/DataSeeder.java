package com.campusconnect.config;

import com.campusconnect.entity.User;
import com.campusconnect.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;

@Configuration
public class DataSeeder {

    private final PasswordEncoder passwordEncoder;

    public DataSeeder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {
            // Always ensure one and only one ADMIN exists — never allow registration for admin
            String adminEmail = "admin@campus.edu";

            boolean adminExists = userRepository.findByEmail(adminEmail).isPresent();

            if (!adminExists) {
                User admin = new User();
                admin.setName("Super Admin");
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(User.Role.ADMIN);
                admin.setDepartment("Administration");
                admin.setAvatar("SA");
                admin.setJoinedAt(LocalDate.now());
                userRepository.save(admin);
                System.out.println("✅ Admin account seeded: " + adminEmail);
            } else {
                System.out.println("✅ Admin account already exists in DB.");
            }
        };
    }
}

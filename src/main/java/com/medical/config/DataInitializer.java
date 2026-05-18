package com.medical.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Check if admin user already exists
        Optional<User> adminOpt = userRepository.findByUsername("admin");

        if (adminOpt.isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // Default password
            admin.setFullName("System Administrator");
            admin.setRole("ROLE_ADMIN");

            userRepository.save(admin);
            System.out.println("Default Admin user created! Username: 'admin', Password: 'admin123'");
        } else {
            // Check if existing admin has a plain text password mistakenly inserted
            User admin = adminOpt.get();
            if (!admin.getPassword().startsWith("$2a$")) {
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ROLE_ADMIN");
                userRepository.save(admin);
                System.out.println("Admin user password was plain text. Successfully re-hashed!");
            }
        }
    }
}

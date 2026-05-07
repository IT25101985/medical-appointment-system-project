package com.medical.config;

import com.medical.entity.User;
import com.medical.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // 1. Simply check if "admin" exists
        if (userRepository.findByUsername("admin").isEmpty()) {

            // 2. Create the Admin user
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // Securely hashed
            admin.setFullName("System Administrator");
            admin.setRole("ROLE_ADMIN");

            // 3. Save to database
            userRepository.save(admin);

            System.out.println(">>> Default Admin created: admin / admin123");
        } else {
            System.out.println(">>> Admin already exists. No action needed.");
        }
    }
}
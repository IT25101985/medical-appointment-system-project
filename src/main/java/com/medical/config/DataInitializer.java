package com.medical.config;

import com.medical.entity.User;
import com.medical.repository.UserRepository;
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

        Optional<User> adminOpt = userRepository.findByUsername("admin");

        // Scenario 1: If admin does not exist, create a new default admin
        if (adminOpt.isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // Hashing the password
            admin.setFullName("System Administrator");
            admin.setRole("ROLE_ADMIN");

            userRepository.save(admin);
            System.out.println("Default Admin created successfully!");
            return; // Exit the method since task is complete
        }

        // Scenario 2: If admin exists, check if the password is in plain text
        User admin = adminOpt.get();
        boolean isPlainText = !admin.getPassword().startsWith("$2a$");

        if (isPlainText) {
            admin.setPassword(passwordEncoder.encode("admin123")); // Securely re-hash the password
            userRepository.save(admin);
            System.out.println("Admin password was plain text. Re-hashed successfully!"); 1
        }
    }
}
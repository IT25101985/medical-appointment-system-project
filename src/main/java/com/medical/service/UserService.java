package com.medical.service;

import com.medical.entity.User;
import com.medical.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User updateUserProfile(User currentUser, User updatedInfo) {
        currentUser.setFullName(updatedInfo.getFullName());
        currentUser.setAddress(updatedInfo.getAddress());
        currentUser.setPhoneNo(updatedInfo.getPhoneNo());
        currentUser.setProfileImage(updatedInfo.getProfileImage());
        return userRepository.save(currentUser);
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }
}

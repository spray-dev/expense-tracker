package com.expensetracker.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.expensetracker.entity.User;
import com.expensetracker.repository.UserRepository;

@Service
public class UserService {
    
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }   

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(
            "User with id " + id + " does not exist"));
    }
    
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException(
            "User with username " + username + " does not exist"));
    }
    
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(
            "User with email " + email + " does not exist"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("User with username " + username + " already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }
        User user = new User(username, password, email);
        return userRepository.save(user);
    }

    public User updateUsername(Long id, String username) {
        User user = getUserById(id);
        if (userRepository.existsByUsernameAndIdNot(username, id)) {
            throw new IllegalArgumentException("User with username " + username + " already exists");
        }
        user.setUsername(username);
        return userRepository.save(user);
    }

    public User updateEmail(Long id, String email) {
        User user = getUserById(id);
        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }
        user.setEmail(email);
        return userRepository.save(user);
    }

    public User updatePassword(Long id, String password) {
        User user = getUserById(id);
        user.setPassword(password);
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
}

package com.expensetracker.service;

import org.springframework.stereotype.Service;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.expensetracker.entity.User;
import com.expensetracker.exception.DuplicateResourceException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;


@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ExpenseRepository expenseRepository, BudgetRepository budgetRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
    }   

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
            "User with id " + id + " does not exist"));
    }

    @Transactional 
    public User createUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("User with username " + username + " already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User with email " + email + " already exists");
        }

        String hashedPassword = passwordEncoder.encode(password);

        User user = new User(username, hashedPassword, email);
        return userRepository.save(user);
    }

    @Transactional 
    public User updateUsername(Long id, String username) {
        User user = getUserById(id);
        if (userRepository.existsByUsernameAndIdNot(username, id)) {
            throw new DuplicateResourceException("User with username " + username + " already exists");
        }
        user.setUsername(username);
        return user;
    }

    @Transactional 
    public User updateEmail(Long id, String email) {
        User user = getUserById(id);
        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateResourceException("User with email " + email + " already exists");
        }
        user.setEmail(email);
        return user;
    }

    @Transactional 
    public User updatePassword(Long id, String password) {
        User user = getUserById(id);
        String hashedPassword = passwordEncoder.encode(password);
        user.setPassword(hashedPassword);
        return user;
    }

    @Transactional 
    public void deleteUser(Long id) {
        User user = getUserById(id);

        expenseRepository.deleteAllByUser_Id(user.getId());
        budgetRepository.deleteAllByUser_Id(user.getId());

        userRepository.delete(user);
    }
}

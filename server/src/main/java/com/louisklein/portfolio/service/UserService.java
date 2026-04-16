package com.louisklein.portfolio.service;

import com.louisklein.portfolio.exception.ResourceNotFoundException;
import com.louisklein.portfolio.model.User;
import com.louisklein.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public Result<User> createUser(String email, String username, String rawPassword) {
        Result<User> result = validateCredentials(email, username, rawPassword);

        if (!result.isSuccess()) {
            return result;
        }

        if (userRepository.existsByEmail(email.trim().toLowerCase())) {
            result.addMessage("Email already in use", ResultType.INVALID);
            return result;
        }

        if (userRepository.existsByUsername(username.trim())) {
            result.addMessage("Username already in use", ResultType.INVALID);
            return result;
        }

        User user = User.builder()
                .email(email.trim().toLowerCase())
                .username(username.trim())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .verified(false)
                .verificationToken(UUID.randomUUID().toString())
                .verificationTokenExpiresAt(OffsetDateTime.now().plusHours(24))
                .build();

        result.setPayload(userRepository.save(user));
        emailService.sendVerificationEmail(email.trim().toLowerCase(), user.getVerificationToken());
        return result;
    }

    public Result<User> updatePassword(UUID id, String rawPassword) {
        Result<User> result = new Result<>();

        if (isNullOrBlank(rawPassword)) {
            result.addMessage("Password is required", ResultType.INVALID);
            return result;
        }

        if (!isValidPassword(rawPassword)) {
            result.addMessage("Password must contain an uppercase letter and a special character", ResultType.INVALID);
            return result;
        }

        if (!isValidLength(rawPassword)) {
            result.addMessage("Password must be 5-20 characters", ResultType.INVALID);
            return result;
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        result.setPayload(userRepository.save(user));
        return result;
    }

    public Result<Void> deleteUser(UUID id) {
        Result<Void> result = new Result<>();

        if (!userRepository.existsById(id)) {
            result.addMessage("User not found", ResultType.NOT_FOUND);
            return result;
        }

        userRepository.deleteById(id);
        return result;
    }

    private Result<User> validateCredentials(String email, String username, String rawPassword) {
        Result<User> result = new Result<>();

        if (isNullOrBlank(email)) {
            result.addMessage("Email is required", ResultType.INVALID);
        } else if (!email.contains("@")) {
            result.addMessage("Email is invalid", ResultType.INVALID);
        }

        if (isNullOrBlank(username)) {
            result.addMessage("Username is required", ResultType.INVALID);
        } else if (!isValidLength(username)) {
            result.addMessage("Username must be 5-20 characters", ResultType.INVALID);
        }

        if (isNullOrBlank(rawPassword)) {
            result.addMessage("Password is required", ResultType.INVALID);
        } else if (!isValidPassword(rawPassword)) {
            result.addMessage("Password must contain an uppercase letter and a special character", ResultType.INVALID);
        } else if (!isValidLength(rawPassword)) {
            result.addMessage("Password must be 5-20 characters", ResultType.INVALID);
        }

        return result;
    }

    public static boolean isNullOrBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean isValidLength(String value) {
        return value.length() >= 5 && value.length() <= 20;
    }

    public static boolean isValidPassword(String value) {
        boolean hasUppercase = value.matches(".*[A-Z].*");
        boolean hasSpecialChar = value.matches(".*[!@#$^&].*");
        boolean hasWhitespace = value.matches(".*\\s.*");
        boolean isValidLength = value.length() >= 5 && value.length() <= 20;

        return hasUppercase && hasSpecialChar && !hasWhitespace && isValidLength;
    }
}
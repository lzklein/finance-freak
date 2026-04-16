package com.louisklein.portfolio.service;

import com.louisklein.portfolio.exception.ResourceNotFoundException;
import com.louisklein.portfolio.model.User;
import com.louisklein.portfolio.repository.UserRepository;
import com.louisklein.portfolio.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    public Result<String> login(String email, String password) {
        Result<String> result = new Result<>();

        if (UserService.isNullOrBlank(email)) {
            result.addMessage("Email is required", ResultType.INVALID);
        }
        if (UserService.isNullOrBlank(password)) {
            result.addMessage("Password is required", ResultType.INVALID);
        }
        if (!result.isSuccess()) {
            return result;
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email.trim().toLowerCase(), password)
            );
        } catch (AuthenticationException e) {
            result.addMessage("Invalid email or password", ResultType.INVALID);
            return result;
        }

        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow();

        String token = jwtUtils.generateToken(user.getEmail());
        result.setPayload(token);
        return result;
    }

    public Result<User> register(String email, String username, String rawPassword) {
        return userService.createUser(email, username, rawPassword);
    }

    public User getUser(String email) {
        return userRepository.findByEmailOrUsername(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public Result<Void> verifyEmail(String token) {
        Result<Void> result = new Result<>();

        User user = userRepository.findByVerificationToken(token)
                .orElse(null);

        if (user == null) {
            result.addMessage("Invalid verification token", ResultType.INVALID);
            return result;
        }

        if (user.getVerificationTokenExpiresAt().isBefore(OffsetDateTime.now())) {
            result.addMessage("Verification token has expired", ResultType.INVALID);
            return result;
        }
        user.setVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiresAt(null);
        userRepository.save(user);

        return result;
    }
}
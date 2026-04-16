package com.louisklein.portfolio.controller;

import com.louisklein.portfolio.dto.AuthResponse;
import com.louisklein.portfolio.dto.LoginRequest;
import com.louisklein.portfolio.dto.RegisterRequest;
import com.louisklein.portfolio.dto.UserResponse;
import com.louisklein.portfolio.model.User;
import com.louisklein.portfolio.security.JwtUtils;
import com.louisklein.portfolio.service.AuthService;
import com.louisklein.portfolio.service.Result;
import com.louisklein.portfolio.service.ResultType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        System.out.println(request);
        Result<User> result = authService.register(
                request.getEmail(),
                request.getUsername(),
                request.getPassword()
        );

        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(result.getMessages());
        }

        User user = result.getPayload();
        String token = jwtUtils.generateToken(user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toAuthResponse(user, token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Result<String> result = authService.login(
                request.getEmail(),
                request.getPassword()
        );

        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(result.getMessages());
        }

        User user = authService.getUser(request.getEmail());
        return ResponseEntity.ok(toAuthResponse(user, result.getPayload()));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam String token) {
        Result<Void> result = authService.verifyEmail(token);

        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(result.getMessages());
        }

        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }

    private AuthResponse toAuthResponse(User user, String token) {
        return new AuthResponse(
                token,
                user.getEmail(),
                user.getUsername()
        );
    }
}
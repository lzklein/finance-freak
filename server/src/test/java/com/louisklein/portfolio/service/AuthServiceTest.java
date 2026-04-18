package com.louisklein.portfolio.service;

import com.louisklein.portfolio.model.User;
import com.louisklein.portfolio.repository.UserRepository;
import com.louisklein.portfolio.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private UserService userService;
    @Mock private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .username("testuser")
                .passwordHash("hashedpassword")
                .verified(true)
                .build();
    }

    // login tests
    @Test
    void login_validCredentials_returnsTokenSuccess() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // fine, since you don't use the return

        when(userRepository.findByEmailOrUsername(anyString()))
                .thenReturn(Optional.of(testUser));

        when(jwtUtils.generateToken(anyString()))
                .thenReturn("fake-jwt-token");

        Result<String> result = authService.login("test@example.com", "Password1!");

        assertTrue(result.isSuccess());
        assertEquals("fake-jwt-token", result.getPayload());
    }

    @Test
    void login_invalidCredentials_returnsInvalid() {
        Result<String> result = authService.login("test@example.com", "wrongpassword");

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
    }

    @Test
    void login_blankEmail_returnsInvalid() {
        Result<String> result = authService.login("", "Password1!");

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void login_blankPassword_returnsInvalid() {
        Result<String> result = authService.login("test@example.com", "");

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
        verify(authenticationManager, never()).authenticate(any());
    }

    // register tests
    @Test
    void register_validInput_returnsSuccess() {
        Result<User> successResult = new Result<>();
        successResult.setPayload(testUser);

        when(userService.createUser(anyString(), anyString(), anyString()))
                .thenReturn(successResult);

        Result<User> result = authService.register("test@example.com", "testuser", "Password1!");

        assertTrue(result.isSuccess());
        assertNotNull(result.getPayload());
    }

    @Test
    void register_duplicateEmail_returnsInvalid() {
        Result<User> failResult = new Result<>();
        failResult.addMessage("Email already in use", ResultType.INVALID);

        when(userService.createUser(anyString(), anyString(), anyString()))
                .thenReturn(failResult);

        Result<User> result = authService.register("test@example.com", "testuser", "Password1!");

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
    }
}
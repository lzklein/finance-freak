package com.louisklein.portfolio.service;

import com.louisklein.portfolio.model.User;
import com.louisklein.portfolio.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

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

    // createUser tests
    @Test
    void createUser_validInput_returnsSuccess() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        Result<User> result = userService.createUser("test@example.com", "testuser", "Password1!");

        assertTrue(result.isSuccess());
        assertNotNull(result.getPayload());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_duplicateEmail_returnsInvalid() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        Result<User> result = userService.createUser("test@example.com", "testuser", "Password1!");

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_duplicateUsername_returnsInvalid() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        Result<User> result = userService.createUser("test@example.com", "testuser", "Password1!");

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_blankEmail_returnsInvalid() {
        Result<User> result = userService.createUser("", "testuser", "Password1!");

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_invalidPassword_returnsInvalid() {
        Result<User> result = userService.createUser("test@example.com", "testuser", "weak");

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
    }

    // findById tests
    @Test
    void findById_existingUser_returnsUser() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));

        User user = userService.findById(testUser.getId());

        assertNotNull(user);
        assertEquals(testUser.getEmail(), user.getEmail());
    }

    @Test
    void findById_nonExistentUser_throwsException() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> userService.findById(UUID.randomUUID()));
    }

    // deleteUser tests
    @Test
    void deleteUser_existingUser_returnsSuccess() {
        when(userRepository.existsById(any(UUID.class))).thenReturn(true);
        doNothing().when(userRepository).deleteById(any(UUID.class));

        Result<Void> result = userService.deleteUser(testUser.getId());

        assertTrue(result.isSuccess());
        verify(userRepository, times(1)).deleteById(testUser.getId());
    }

    @Test
    void deleteUser_nonExistentUser_returnsNotFound() {
        when(userRepository.existsById(any(UUID.class))).thenReturn(false);

        Result<Void> result = userService.deleteUser(UUID.randomUUID());

        assertFalse(result.isSuccess());
        assertEquals(ResultType.NOT_FOUND, result.getType());
        verify(userRepository, never()).deleteById(any(UUID.class));
    }

    // updatePassword tests
    @Test
    void updatePassword_validPassword_returnsSuccess() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("newhashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        Result<User> result = userService.updatePassword(testUser.getId(), "NewPass1!");

        assertTrue(result.isSuccess());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updatePassword_blankPassword_returnsInvalid() {
        Result<User> result = userService.updatePassword(testUser.getId(), "");

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
        verify(userRepository, never()).save(any(User.class));
    }
}
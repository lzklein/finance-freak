package com.louisklein.portfolio.repository;

import com.louisklein.portfolio.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .username("testuser")
                .passwordHash("hashedpassword")
                .verified(true)
                .build();
        entityManager.persistAndFlush(testUser);
    }

    @Test
    void findByEmail_existingEmail_returnsUser() {
        Optional<User> result = userRepository.findByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void findByEmail_nonExistentEmail_returnsEmpty() {
        Optional<User> result = userRepository.findByEmail("nobody@example.com");

        assertFalse(result.isPresent());
    }

    @Test
    void findByUsername_existingUsername_returnsUser() {
        Optional<User> result = userRepository.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void existsByEmail_existingEmail_returnsTrue() {
        assertTrue(userRepository.existsByEmail("test@example.com"));
    }

    @Test
    void existsByEmail_nonExistentEmail_returnsFalse() {
        assertFalse(userRepository.existsByEmail("nobody@example.com"));
    }

    @Test
    void existsByUsername_existingUsername_returnsTrue() {
        assertTrue(userRepository.existsByUsername("testuser"));
    }

    @Test
    void existsByUsername_nonExistentUsername_returnsFalse() {
        assertFalse(userRepository.existsByUsername("nobody"));
    }

    @Test
    void findByVerificationToken_existingToken_returnsUser() {
        User unverifiedUser = User.builder()
                .email("unverified@example.com")
                .username("unverified")
                .passwordHash("hashedpassword")
                .verified(false)
                .verificationToken("test-token-123")
                .build();
        entityManager.persistAndFlush(unverifiedUser);

        Optional<User> result = userRepository.findByVerificationToken("test-token-123");

        assertTrue(result.isPresent());
        assertEquals("unverified@example.com", result.get().getEmail());
    }

    @Test
    void save_newUser_persistsToDatabase() {
        User newUser = User.builder()
                .email("new@example.com")
                .username("newuser")
                .passwordHash("hashedpassword")
                .verified(true)
                .build();

        User saved = userRepository.save(newUser);

        assertNotNull(saved.getId());
        assertEquals("new@example.com", saved.getEmail());
    }

    @Test
    void deleteById_existingUser_removesFromDatabase() {
        userRepository.deleteById(testUser.getId());

        Optional<User> result = userRepository.findById(testUser.getId());
        assertFalse(result.isPresent());
    }
}
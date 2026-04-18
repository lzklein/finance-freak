package com.louisklein.portfolio.repository;

import com.louisklein.portfolio.model.User;
import com.louisklein.portfolio.model.Watchlist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class WatchlistRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WatchlistRepository watchlistRepository;

    private User testUser;
    private Watchlist testWatchlist;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .username("testuser")
                .passwordHash("hashedpassword")
                .verified(true)
                .build();
        entityManager.persistAndFlush(testUser);

        testWatchlist = Watchlist.builder()
                .user(testUser)
                .name("My Watchlist")
                .build();
        entityManager.persistAndFlush(testWatchlist);
    }

    @Test
    void findByUserId_existingUser_returnsWatchlists() {
        List<Watchlist> results = watchlistRepository.findByUserId(testUser.getId());

        assertEquals(1, results.size());
        assertEquals("My Watchlist", results.get(0).getName());
    }

    @Test
    void findByUserId_noWatchlists_returnsEmpty() {
        User otherUser = User.builder()
                .email("other@example.com")
                .username("otheruser")
                .passwordHash("hashedpassword")
                .verified(true)
                .build();
        entityManager.persistAndFlush(otherUser);

        List<Watchlist> results = watchlistRepository.findByUserId(otherUser.getId());

        assertTrue(results.isEmpty());
    }

    @Test
    void existsByUserIdAndName_existingName_returnsTrue() {
        assertTrue(watchlistRepository.existsByUserIdAndName(testUser.getId(), "My Watchlist"));
    }

    @Test
    void existsByUserIdAndName_nonExistentName_returnsFalse() {
        assertFalse(watchlistRepository.existsByUserIdAndName(testUser.getId(), "Other Watchlist"));
    }

    @Test
    void existsByUserIdAndName_differentUser_returnsFalse() {
        User otherUser = User.builder()
                .email("other@example.com")
                .username("otheruser")
                .passwordHash("hashedpassword")
                .verified(true)
                .build();
        entityManager.persistAndFlush(otherUser);

        assertFalse(watchlistRepository.existsByUserIdAndName(otherUser.getId(), "My Watchlist"));
    }
}
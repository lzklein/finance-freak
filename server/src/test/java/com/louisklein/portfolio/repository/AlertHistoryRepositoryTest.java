package com.louisklein.portfolio.repository;

import com.louisklein.portfolio.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AlertHistoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AlertHistoryRepository alertHistoryRepository;

    private User testUser;
    private Alert testAlert;
    private AlertHistory testHistory;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .username("testuser")
                .passwordHash("hashedpassword")
                .verified(true)
                .build();
        entityManager.persistAndFlush(testUser);

        Asset testAsset = Asset.builder()
                .name("AK-47 | Redline (Field-Tested)")
                .assetType(Asset.AssetType.CS2_SKIN)
                .build();
        entityManager.persistAndFlush(testAsset);

        testAlert = Alert.builder()
                .user(testUser)
                .asset(testAsset)
                .condition(Alert.AlertCondition.ABOVE)
                .threshold(new BigDecimal("50.00"))
                .priceType(Alert.PriceType.LAST)
                .active(true)
                .build();
        entityManager.persistAndFlush(testAlert);

        testHistory = AlertHistory.builder()
                .alert(testAlert)
                .user(testUser)
                .triggeredPrice(new BigDecimal("55.00"))
                .build();
        entityManager.persistAndFlush(testHistory);
    }

    @Test
    void findByUserId_existingUser_returnsHistory() {
        List<AlertHistory> results = alertHistoryRepository.findByUserId(testUser.getId());

        assertEquals(1, results.size());
        assertEquals(new BigDecimal("55.00"), results.get(0).getTriggeredPrice());
    }

    @Test
    void findByAlertId_existingAlert_returnsHistory() {
        List<AlertHistory> results = alertHistoryRepository.findByAlertId(testAlert.getId());

        assertEquals(1, results.size());
    }

    @Test
    void findByUserIdOrderByTriggeredAtDesc_returnsOrderedHistory() {
        AlertHistory secondHistory = AlertHistory.builder()
                .alert(testAlert)
                .user(testUser)
                .triggeredPrice(new BigDecimal("60.00"))
                .build();
        entityManager.persistAndFlush(secondHistory);

        List<AlertHistory> results = alertHistoryRepository
                .findByUserIdOrderByTriggeredAtDesc(testUser.getId());

        assertEquals(2, results.size());
        assertTrue(results.get(0).getTriggeredPrice()
                .compareTo(results.get(1).getTriggeredPrice()) >= 0);
    }

    @Test
    void findByUserId_noHistory_returnsEmpty() {
        User otherUser = User.builder()
                .email("other@example.com")
                .username("otheruser")
                .passwordHash("hashedpassword")
                .verified(true)
                .build();
        entityManager.persistAndFlush(otherUser);

        List<AlertHistory> results = alertHistoryRepository.findByUserId(otherUser.getId());

        assertTrue(results.isEmpty());
    }
}
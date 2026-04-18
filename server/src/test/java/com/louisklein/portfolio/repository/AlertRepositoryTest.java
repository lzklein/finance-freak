package com.louisklein.portfolio.repository;

import com.louisklein.portfolio.model.Alert;
import com.louisklein.portfolio.model.Asset;
import com.louisklein.portfolio.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AlertRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AlertRepository alertRepository;

    private User testUser;
    private Asset testAsset;
    private Alert testAlert;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .username("testuser")
                .passwordHash("hashedpassword")
                .verified(true)
                .build();
        entityManager.persistAndFlush(testUser);

        testAsset = Asset.builder()
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
    }

    @Test
    void findByUserId_existingUser_returnsAlerts() {
        List<Alert> results = alertRepository.findByUserId(testUser.getId());

        assertEquals(1, results.size());
        assertEquals(Alert.AlertCondition.ABOVE, results.get(0).getCondition());
    }

    @Test
    void findByUserIdAndActive_activeAlerts_returnsActive() {
        List<Alert> results = alertRepository.findByUserIdAndActive(testUser.getId(), true);

        assertEquals(1, results.size());
        assertTrue(results.get(0).isActive());
    }

    @Test
    void findByUserIdAndActive_inactiveAlerts_returnsEmpty() {
        List<Alert> results = alertRepository.findByUserIdAndActive(testUser.getId(), false);

        assertTrue(results.isEmpty());
    }

    @Test
    void findByAssetId_existingAsset_returnsAlerts() {
        List<Alert> results = alertRepository.findByAssetId(testAsset.getId());

        assertEquals(1, results.size());
    }

    @Test
    void findByActive_activeAlerts_returnsAll() {
        List<Alert> results = alertRepository.findByActive(true);

        assertEquals(1, results.size());
    }

    @Test
    void existsByUserIdAndAssetIdAndCondition_existingAlert_returnsTrue() {
        assertTrue(alertRepository.existsByUserIdAndAssetIdAndCondition(
                testUser.getId(), testAsset.getId(), Alert.AlertCondition.ABOVE));
    }

    @Test
    void existsByUserIdAndAssetIdAndCondition_differentCondition_returnsFalse() {
        assertFalse(alertRepository.existsByUserIdAndAssetIdAndCondition(
                testUser.getId(), testAsset.getId(), Alert.AlertCondition.BELOW));
    }
}
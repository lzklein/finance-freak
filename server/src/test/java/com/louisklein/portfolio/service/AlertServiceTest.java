package com.louisklein.portfolio.service;

import com.louisklein.portfolio.exception.ResourceNotFoundException;
import com.louisklein.portfolio.model.*;
import com.louisklein.portfolio.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock private AlertRepository alertRepository;
    @Mock private AlertHistoryRepository alertHistoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private AssetRepository assetRepository;

    @InjectMocks
    private AlertService alertService;

    private User testUser;
    private Asset testAsset;
    private Alert testAlert;
    private UUID userId;
    private UUID assetId;
    private UUID alertId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        assetId = UUID.randomUUID();
        alertId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .username("testuser")
                .build();

        testAsset = Asset.builder()
                .id(assetId)
                .name("AK-47 | Redline (Field-Tested)")
                .assetType(Asset.AssetType.CS2_SKIN)
                .build();

        testAlert = Alert.builder()
                .id(alertId)
                .user(testUser)
                .asset(testAsset)
                .condition(Alert.AlertCondition.ABOVE)
                .threshold(new BigDecimal("50.00"))
                .active(true)
                .build();
    }

    // createAlert tests
    @Test
    void createAlert_validInput_returnsSuccess() {
        when(alertRepository.existsByUserIdAndAssetIdAndCondition(any(), any(), any())).thenReturn(false);
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(assetRepository.findById(any(UUID.class))).thenReturn(Optional.of(testAsset));
        when(alertRepository.save(any(Alert.class))).thenReturn(testAlert);

        Result<Alert> result = alertService.createAlert(
                userId, assetId, Alert.AlertCondition.ABOVE, new BigDecimal("50.00"));

        assertTrue(result.isSuccess());
        assertNotNull(result.getPayload());
        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    void createAlert_nullCondition_returnsInvalid() {
        Result<Alert> result = alertService.createAlert(
                userId, assetId, null, new BigDecimal("50.00"));

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
        verify(alertRepository, never()).save(any(Alert.class));
    }

    @Test
    void createAlert_nullThreshold_returnsInvalid() {
        Result<Alert> result = alertService.createAlert(
                userId, assetId, Alert.AlertCondition.ABOVE, null);

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
        verify(alertRepository, never()).save(any(Alert.class));
    }

    @Test
    void createAlert_zeroThreshold_returnsInvalid() {
        Result<Alert> result = alertService.createAlert(
                userId, assetId, Alert.AlertCondition.ABOVE, BigDecimal.ZERO);

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
        verify(alertRepository, never()).save(any(Alert.class));
    }

    @Test
    void createAlert_duplicateAlert_returnsInvalid() {
        when(alertRepository.existsByUserIdAndAssetIdAndCondition(any(), any(), any())).thenReturn(true);

        Result<Alert> result = alertService.createAlert(
                userId, assetId, Alert.AlertCondition.ABOVE, new BigDecimal("50.00"));

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
        verify(alertRepository, never()).save(any(Alert.class));
    }

    // findByUserId tests
    @Test
    void findByUserId_returnsAlerts() {
        when(alertRepository.findByUserId(any(UUID.class))).thenReturn(List.of(testAlert));

        List<Alert> results = alertService.findByUserId(userId);

        assertEquals(1, results.size());
        assertEquals(Alert.AlertCondition.ABOVE, results.get(0).getCondition());
    }

    @Test
    void findActiveByUserId_returnsActiveAlerts() {
        when(alertRepository.findByUserIdAndActive(any(UUID.class), eq(true)))
                .thenReturn(List.of(testAlert));

        List<Alert> results = alertService.findActiveByUserId(userId);

        assertEquals(1, results.size());
        assertTrue(results.get(0).isActive());
    }

    // updateAlert tests
    @Test
    void updateAlert_validInput_returnsSuccess() {
        when(alertRepository.findById(any(UUID.class))).thenReturn(Optional.of(testAlert));
        when(alertRepository.save(any(Alert.class))).thenReturn(testAlert);

        Result<Alert> result = alertService.updateAlert(
                alertId, userId, Alert.AlertCondition.BELOW, new BigDecimal("40.00"));

        assertTrue(result.isSuccess());
        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    void updateAlert_wrongUser_returnsInvalid() {
        when(alertRepository.findById(any(UUID.class))).thenReturn(Optional.of(testAlert));

        Result<Alert> result = alertService.updateAlert(
                alertId, UUID.randomUUID(), Alert.AlertCondition.BELOW, new BigDecimal("40.00"));

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
        verify(alertRepository, never()).save(any(Alert.class));
    }

    @Test
    void updateAlert_notFound_throwsException() {
        when(alertRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> alertService.updateAlert(alertId, userId,
                        Alert.AlertCondition.BELOW, new BigDecimal("40.00")));
    }

    // toggleAlert tests
    @Test
    void toggleAlert_activeAlert_deactivates() {
        when(alertRepository.findById(any(UUID.class))).thenReturn(Optional.of(testAlert));
        when(alertRepository.save(any(Alert.class))).thenReturn(testAlert);

        Result<Alert> result = alertService.toggleAlert(alertId, userId);

        assertTrue(result.isSuccess());
        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    void toggleAlert_wrongUser_returnsInvalid() {
        when(alertRepository.findById(any(UUID.class))).thenReturn(Optional.of(testAlert));

        Result<Alert> result = alertService.toggleAlert(alertId, UUID.randomUUID());

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
        verify(alertRepository, never()).save(any(Alert.class));
    }

    // deleteAlert tests
    @Test
    void deleteAlert_ownerDeletes_returnsSuccess() {
        when(alertRepository.findById(any(UUID.class))).thenReturn(Optional.of(testAlert));
        doNothing().when(alertRepository).deleteById(any(UUID.class));

        Result<Void> result = alertService.deleteAlert(alertId, userId);

        assertTrue(result.isSuccess());
        verify(alertRepository, times(1)).deleteById(alertId);
    }

    @Test
    void deleteAlert_wrongUser_returnsInvalid() {
        when(alertRepository.findById(any(UUID.class))).thenReturn(Optional.of(testAlert));

        Result<Void> result = alertService.deleteAlert(alertId, UUID.randomUUID());

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
        verify(alertRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void deleteAlert_notFound_throwsException() {
        when(alertRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> alertService.deleteAlert(alertId, userId));
    }

    // triggerAlert tests
    @Test
    void triggerAlert_savesHistoryAndUpdatesAlert() {
        when(alertHistoryRepository.save(any(AlertHistory.class))).thenReturn(new AlertHistory());
        when(alertRepository.save(any(Alert.class))).thenReturn(testAlert);

        alertService.triggerAlert(testAlert, new BigDecimal("55.00"));

        verify(alertHistoryRepository, times(1)).save(any(AlertHistory.class));
        verify(alertRepository, times(1)).save(any(Alert.class));
    }
}
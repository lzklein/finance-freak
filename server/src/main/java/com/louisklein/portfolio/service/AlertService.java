package com.louisklein.portfolio.service;

import com.louisklein.portfolio.exception.ResourceNotFoundException;
import com.louisklein.portfolio.model.Alert;
import com.louisklein.portfolio.model.AlertHistory;
import com.louisklein.portfolio.model.Asset;
import com.louisklein.portfolio.model.User;
import com.louisklein.portfolio.repository.AlertHistoryRepository;
import com.louisklein.portfolio.repository.AlertRepository;
import com.louisklein.portfolio.repository.AssetRepository;
import com.louisklein.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final AlertHistoryRepository alertHistoryRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;

    public List<Alert> findByUserId(UUID userId) {
        return alertRepository.findByUserId(userId);
    }

    public List<Alert> findActiveByUserId(UUID userId) {
        return alertRepository.findByUserIdAndActive(userId, true);
    }

    public List<AlertHistory> findHistoryByUserId(UUID userId) {
        return alertHistoryRepository.findByUserIdOrderByTriggeredAtDesc(userId);
    }

    public Result<Alert> createAlert(UUID userId, UUID assetId,
                                     Alert.AlertCondition condition, BigDecimal threshold) {
        Result<Alert> result = new Result<>();

        if (condition == null) {
            result.addMessage("Condition is required", ResultType.INVALID);
        }

        if (threshold == null) {
            result.addMessage("Threshold is required", ResultType.INVALID);
        } else if (threshold.compareTo(BigDecimal.ZERO) <= 0) {
            result.addMessage("Threshold must be greater than zero", ResultType.INVALID);
        }

        if (!result.isSuccess()) {
            return result;
        }

        if (alertRepository.existsByUserIdAndAssetIdAndCondition(userId, assetId, condition)) {
            result.addMessage("You already have a " + condition + " alert for this asset", ResultType.INVALID);
            return result;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

        Alert alert = Alert.builder()
                .user(user)
                .asset(asset)
                .condition(condition)
                .threshold(threshold)
                .active(true)
                .build();

        result.setPayload(alertRepository.save(alert));
        return result;
    }

    public Result<Alert> updateAlert(UUID alertId, UUID userId,
                                     Alert.AlertCondition condition, BigDecimal threshold) {
        Result<Alert> result = new Result<>();

        if (condition == null) {
            result.addMessage("Condition is required", ResultType.INVALID);
        }

        if (threshold == null) {
            result.addMessage("Threshold is required", ResultType.INVALID);
        } else if (threshold.compareTo(BigDecimal.ZERO) <= 0) {
            result.addMessage("Threshold must be greater than zero", ResultType.INVALID);
        }

        if (!result.isSuccess()) {
            return result;
        }

        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        if (!alert.getUser().getId().equals(userId)) {
            result.addMessage("You do not have permission to modify this alert", ResultType.INVALID);
            return result;
        }

        alert.setCondition(condition);
        alert.setThreshold(threshold);
        result.setPayload(alertRepository.save(alert));
        return result;
    }

    public Result<Alert> toggleAlert(UUID alertId, UUID userId) {
        Result<Alert> result = new Result<>();

        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        if (!alert.getUser().getId().equals(userId)) {
            result.addMessage("You do not have permission to modify this alert", ResultType.INVALID);
            return result;
        }

        alert.setActive(!alert.isActive());
        result.setPayload(alertRepository.save(alert));
        return result;
    }

    public Result<Void> deleteAlert(UUID alertId, UUID userId) {
        Result<Void> result = new Result<>();

        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        if (!alert.getUser().getId().equals(userId)) {
            result.addMessage("You do not have permission to delete this alert", ResultType.INVALID);
            return result;
        }

        alertRepository.deleteById(alertId);
        return result;
    }

    public void triggerAlert(Alert alert, BigDecimal triggeredPrice) {
        AlertHistory history = AlertHistory.builder()
                .alert(alert)
                .user(alert.getUser())
                .triggeredPrice(triggeredPrice)
                .build();

        alertHistoryRepository.save(history);

        alert.setLastTriggeredAt(java.time.OffsetDateTime.now());
        alertRepository.save(alert);
    }
}
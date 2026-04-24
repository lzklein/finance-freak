package com.louisklein.portfolio.controller;

import com.louisklein.portfolio.dto.AlertRequest;
import com.louisklein.portfolio.dto.AlertHistoryResponse;
import com.louisklein.portfolio.dto.AlertResponse;
import com.louisklein.portfolio.model.Alert;
import com.louisklein.portfolio.model.AlertHistory;
import com.louisklein.portfolio.model.User;
import com.louisklein.portfolio.service.AlertService;
import com.louisklein.portfolio.service.Result;
import com.louisklein.portfolio.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<AlertResponse>> getAlerts(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        List<Alert> alerts = alertService.findByUserId(user.getId());
        return ResponseEntity.ok(alerts.stream().map(this::toResponse).toList());
    }

    @GetMapping("/active")
    public ResponseEntity<List<AlertResponse>> getActiveAlerts(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        List<Alert> alerts = alertService.findActiveByUserId(user.getId());
        return ResponseEntity.ok(alerts.stream().map(this::toResponse).toList());
    }

    @GetMapping("/history")
    public ResponseEntity<List<AlertHistoryResponse>> getAlertHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        List<AlertHistory> history = alertService.findHistoryByUserId(user.getId());
        return ResponseEntity.ok(history.stream().map(this::toHistoryResponse).toList());
    }

    @PostMapping
    public ResponseEntity<?> createAlert(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AlertRequest request) {
        User user = userService.findByEmail(userDetails.getUsername());
        Result<Alert> result = alertService.createAlert(
                user.getId(),
                request.getAssetId(),
                request.getCondition(),
                request.getThreshold()
        );

        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(result.getMessages());
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(result.getPayload()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateAlert(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @RequestBody AlertRequest request) {
        User user = userService.findByEmail(userDetails.getUsername());
        Result<Alert> result = alertService.updateAlert(
                id,
                user.getId(),
                request.getCondition(),
                request.getThreshold()
        );

        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(result.getMessages());
        }

        return ResponseEntity.ok(toResponse(result.getPayload()));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleAlert(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        User user = userService.findByEmail(userDetails.getUsername());
        Result<Alert> result = alertService.toggleAlert(id, user.getId());

        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(result.getMessages());
        }

        return ResponseEntity.ok(toResponse(result.getPayload()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAlert(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        User user = userService.findByEmail(userDetails.getUsername());
        Result<Void> result = alertService.deleteAlert(id, user.getId());

        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(result.getMessages());
        }

        return ResponseEntity.noContent().build();
    }

    private AlertResponse toResponse(Alert alert) {
        AlertResponse response = new AlertResponse();
        response.setId(alert.getId());
        response.setAssetId(alert.getAsset().getId());
        response.setAssetName(alert.getAsset().getName());
        response.setCondition(alert.getCondition());
        response.setThreshold(alert.getThreshold());
        response.setActive(alert.isActive());
        response.setCreatedAt(alert.getCreatedAt());
        response.setLastTriggeredAt(alert.getLastTriggeredAt());
        return response;
    }

    private AlertHistoryResponse toHistoryResponse(AlertHistory history) {
        AlertHistoryResponse response = new AlertHistoryResponse();
        response.setId(history.getId());
        response.setAlertId(history.getAlert().getId());
        response.setAssetId(history.getAlert().getAsset().getId());
        response.setAssetName(history.getAlert().getAsset().getName());
        response.setTriggeredPrice(history.getTriggeredPrice());
        response.setTriggeredAt(history.getTriggeredAt());
        return response;
    }
}
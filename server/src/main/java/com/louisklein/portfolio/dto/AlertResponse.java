package com.louisklein.portfolio.dto;

import com.louisklein.portfolio.model.Alert;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class AlertResponse {
    private UUID id;
    private UUID assetId;
    private String assetName;
    private Alert.AlertCondition condition;
    private BigDecimal threshold;
    private boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastTriggeredAt;
}
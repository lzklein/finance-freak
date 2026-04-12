package com.louisklein.portfolio.dto;

import com.louisklein.portfolio.model.Alert;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AlertRequest {
    private UUID assetId;
    private Alert.AlertCondition condition;
    private BigDecimal threshold;
}
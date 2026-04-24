package com.louisklein.portfolio.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class AlertHistoryResponse {
    private UUID id;
    private UUID alertId;
    private UUID assetId;
    private String assetName;
    private BigDecimal triggeredPrice;
    private OffsetDateTime triggeredAt;
}
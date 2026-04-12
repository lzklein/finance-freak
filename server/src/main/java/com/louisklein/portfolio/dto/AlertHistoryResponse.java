package com.louisklein.portfolio.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class AlertHistoryResponse {
    private UUID id;
    private UUID alertId;
    private String assetSymbol;
    private BigDecimal triggeredPrice;
    private OffsetDateTime triggeredAt;
}
package com.louisklein.portfolio.dto;

import com.louisklein.portfolio.model.Asset;
import lombok.Data;
import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AssetResponse {
    private UUID id;
    private String symbol;
    private String name;
    private Asset.AssetType assetType;
    private String exchange;
    private BigDecimal currentPrice;
    private BigDecimal changePct24h;
    private OffsetDateTime createdAt;
}
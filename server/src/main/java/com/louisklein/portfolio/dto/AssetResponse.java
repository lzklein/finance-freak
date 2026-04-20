package com.louisklein.portfolio.dto;

import com.louisklein.portfolio.model.Asset;
import lombok.Data;
import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
public class AssetResponse {
    private UUID id;
    private String name;
    private Asset.AssetType assetType;
    private String imageUrl;
    private String weaponType;
    private Map<String, PriceResponse> prices;
    private BigDecimal changePct;
    private OffsetDateTime createdAt;
}
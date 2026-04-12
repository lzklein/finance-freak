package com.louisklein.portfolio.dto;

import com.louisklein.portfolio.model.Asset;
import lombok.Data;

@Data
public class AssetRequest {
    private String symbol;
    private String name;
    private Asset.AssetType assetType;
    private String exchange;
}
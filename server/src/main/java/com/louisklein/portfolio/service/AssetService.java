package com.louisklein.portfolio.service;

import com.louisklein.portfolio.exception.ResourceNotFoundException;
import com.louisklein.portfolio.model.Asset;
import com.louisklein.portfolio.repository.AssetRepository;
import com.louisklein.portfolio.repository.PriceCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final PriceCacheRepository priceCacheRepository;

    public List<Asset> findAll() {
        return assetRepository.findAll();
    }

    public Asset findById(UUID id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
    }

    public Asset findByIdWithPrice(UUID id) {
        return assetRepository.findByIdWithPrice(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
    }

    public Asset findBySymbol(String symbol) {
        return assetRepository.findBySymbol(symbol.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + symbol));
    }

    public List<Asset> findByName(String name) {
        return assetRepository.findByNameContaining(name.trim().toLowerCase());
    }

    public List<Asset> findByAssetType(Asset.AssetType assetType) {
        return assetRepository.findByAssetType(assetType);
    }

    public Result<Asset> createAsset(String symbol, String name, Asset.AssetType assetType, String exchange) {
        Result<Asset> result = new Result<>();

        if (UserService.isNullOrBlank(symbol)) {
            result.addMessage("Symbol is required", ResultType.INVALID);
        } else if (symbol.length() > 20) {
            result.addMessage("Symbol must be 20 characters or less", ResultType.INVALID);
        } else if (assetRepository.existsBySymbol(symbol.trim().toUpperCase())) {
            result.addMessage("Asset with symbol " + symbol + " already exists", ResultType.INVALID);
        }

        if (UserService.isNullOrBlank(name)) {
            result.addMessage("Name is required", ResultType.INVALID);
        }

        if (assetType == null) {
            result.addMessage("Asset type is required", ResultType.INVALID);
        }

        if (!result.isSuccess()) {
            return result;
        }

        Asset asset = Asset.builder()
                .symbol(symbol.trim().toUpperCase())
                .name(name.trim())
                .assetType(assetType)
                .exchange(exchange != null ? exchange.trim() : null)
                .build();

        result.setPayload(assetRepository.save(asset));
        return result;
    }

    public Result<Asset> updateAsset(UUID id, String name, String exchange) {
        Result<Asset> result = new Result<>();

        if (UserService.isNullOrBlank(name)) {
            result.addMessage("Name is required", ResultType.INVALID);
            return result;
        }

        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

        asset.setName(name.trim());
        asset.setExchange(exchange != null ? exchange.trim() : null);
        result.setPayload(assetRepository.save(asset));
        return result;
    }

    public Result<Void> deleteAsset(UUID id) {
        Result<Void> result = new Result<>();

        if (!assetRepository.existsById(id)) {
            result.addMessage("Asset not found", ResultType.NOT_FOUND);
            return result;
        }

        assetRepository.deleteById(id);
        return result;
    }
}
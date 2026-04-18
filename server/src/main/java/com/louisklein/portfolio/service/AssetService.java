package com.louisklein.portfolio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.louisklein.portfolio.exception.ResourceNotFoundException;
import com.louisklein.portfolio.model.Asset;
import com.louisklein.portfolio.repository.AssetRepository;
import com.louisklein.portfolio.repository.PriceCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final PriceCacheRepository priceCacheRepository;
    private final AlpacaClient alpacaClient;
    private final SteamMarketClient steamMarketClient;

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



    public List<Asset> findByName(String name) {
        return assetRepository.findByNameContaining(name.trim().toLowerCase());
    }

    public List<Asset> findByAssetType(Asset.AssetType assetType) {
        return assetRepository.findByAssetType(assetType);
    }

    public Result<Asset> createAsset(String name, Asset.AssetType assetType, String imageUrl) {
        Result<Asset> result = new Result<>();

        if (UserService.isNullOrBlank(name)) {
            result.addMessage("Name is required", ResultType.INVALID);
        } else if (assetRepository.existsByName(name.trim())) {
            result.addMessage("Asset already exists", ResultType.INVALID);
        }

        if (assetType == null) {
            result.addMessage("Asset type is required", ResultType.INVALID);
        }

        if (!result.isSuccess()) return result;

        Asset asset = Asset.builder()
                .name(name.trim())
                .assetType(assetType)
                .imageUrl(imageUrl)
                .build();

        result.setPayload(assetRepository.save(asset));
        return result;
    }

    public Result<Asset> updateAsset(UUID id, String name, String imageUrl) {
        Result<Asset> result = new Result<>();

        if (UserService.isNullOrBlank(name)) {
            result.addMessage("Name is required", ResultType.INVALID);
            return result;
        }

        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

        asset.setName(name.trim());
        asset.setImageUrl(imageUrl);
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

    public Map<String, Object> searchFromSteam(String query, int page) {
        int count = 20;
        int start = page * count;

        JsonNode response = steamMarketClient.searchItems(query, start, count);
        List<Asset> results = new ArrayList<>();

        if (response == null || !response.path("success").asBoolean()) {
            return Map.of("results", results, "totalCount", 0);
        }

        int totalCount = response.path("total_count").asInt();
        JsonNode items = response.path("results");
        if (!items.isArray()) return Map.of("results", results, "totalCount", 0);

        for (JsonNode item : items) {
            String name = item.path("name").asText();
            String imageUrl = "https://community.akamai.steamstatic.com/economy/image/"
                    + item.path("asset_description").path("icon_url").asText();

            Optional<Asset> existing = assetRepository.findByName(name);
            Asset asset = existing.orElse(Asset.builder()
                    .name(name)
                    .assetType(Asset.AssetType.CS2_SKIN)
                    .imageUrl(imageUrl)
                    .build());

            results.add(asset);
        }

        return Map.of("results", results, "totalCount", totalCount);
    }

//    public List<Asset> searchFromAlpaca(String query) {
//        List<Asset> results = new ArrayList<>();
//
//        System.out.println("Stocks");
//        JsonNode stocks = alpacaClient.searchAssets(query);
//        System.out.println("Crypto");
//        JsonNode crypto = alpacaClient.searchCryptoAssets(query);
//
//        if (stocks != null && stocks.isArray()) {
//            for (JsonNode node : stocks) {
//                results.add(mapToAsset(node, Asset.AssetType.STOCK));
//            }
//        }
//        System.out.println(results);
//
//        if (crypto != null && crypto.isArray()) {
//            for (JsonNode node : crypto) {
//                results.add(mapToAsset(node, Asset.AssetType.CRYPTO));
//            }
//        }
//
//        return results;
//    }

//  }
}
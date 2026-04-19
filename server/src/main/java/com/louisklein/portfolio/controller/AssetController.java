package com.louisklein.portfolio.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.louisklein.portfolio.dto.AssetRequest;
import com.louisklein.portfolio.dto.AssetResponse;
import com.louisklein.portfolio.dto.PriceResponse;
import com.louisklein.portfolio.model.Asset;
import com.louisklein.portfolio.model.PriceCache;
import com.louisklein.portfolio.repository.PriceCacheRepository;
import com.louisklein.portfolio.service.AlpacaClient;
import com.louisklein.portfolio.service.AssetService;
import com.louisklein.portfolio.service.Result;
import com.louisklein.portfolio.service.SteamMarketClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;
    private final PriceCacheRepository priceCacheRepository;
    private final AlpacaClient alpacaClient;
    private final SteamMarketClient steamMarketClient;

    @GetMapping
    public ResponseEntity<List<AssetResponse>> getAllAssets() {
        List<Asset> assets = assetService.findAll();
        return ResponseEntity.ok(assets.stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetResponse> getAssetById(@PathVariable UUID id) {
        Asset asset = assetService.findByIdWithPrice(id);
        return ResponseEntity.ok(toResponse(asset));
    }

//    @GetMapping("/symbol/{symbol}")
//    public ResponseEntity<AssetResponse> getAssetBySymbol(@PathVariable String symbol) {
//        Asset asset = assetService.findBySymbol(symbol);
//        return ResponseEntity.ok(toResponse(asset));
//    }

    @GetMapping("/search")
    public ResponseEntity<List<AssetResponse>> searchAssets(@RequestParam String name) {
        List<Asset> assets = assetService.findByName(name);
        return ResponseEntity.ok(assets.stream().map(this::toResponse).toList());
    }

    @GetMapping("/type/{assetType}")
    public ResponseEntity<List<AssetResponse>> getAssetsByType(@PathVariable Asset.AssetType assetType) {
        List<Asset> assets = assetService.findByAssetType(assetType);
        return ResponseEntity.ok(assets.stream().map(this::toResponse).toList());
    }

    @PostMapping
    public ResponseEntity<?> createAsset(@RequestBody AssetRequest request) {
        Result<Asset> result = assetService.createAsset(
                request.getName(),
                request.getAssetType(),
                request.getImageUrl()
        );

        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(result.getMessages());
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(result.getPayload()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateAsset(@PathVariable UUID id,
                                         @RequestBody AssetRequest request) {
        Result<Asset> result = assetService.updateAsset(id, request.getName(), request.getImageUrl());

        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(result.getMessages());
        }

        return ResponseEntity.ok(toResponse(result.getPayload()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAsset(@PathVariable UUID id) {
        Result<Void> result = assetService.deleteAsset(id);

        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(result.getMessages());
        }

        return ResponseEntity.noContent().build();
    }

    private AssetResponse toResponse(Asset asset) {
        AssetResponse response = new AssetResponse();
        response.setId(asset.getId());
        response.setName(asset.getName());
        response.setAssetType(asset.getAssetType());
        response.setImageUrl(asset.getImageUrl());
        response.setCreatedAt(asset.getCreatedAt());
        if (asset.getSkinDetails() != null) {
            response.setWeaponType(asset.getSkinDetails().getWeaponType());
        }

        List<PriceCache> prices = priceCacheRepository.findAllByAssetId(asset.getId());
        Map<String, PriceResponse> priceMap = new HashMap<>();
        for (PriceCache pc : prices) {
            PriceResponse pr = new PriceResponse();
            pr.setLowestAsk(pc.getLowestAsk());
            pr.setHighestBid(pc.getHighestBid());
            pr.setLastSale(pc.getLastSale());
            pr.setVolume24h(pc.getVolume24h());
            pr.setFetchedAt(pc.getFetchedAt());
            priceMap.put(pc.getMarketplace().name(), pr);
        }
        response.setPrices(priceMap);
        return response;
    }
//    @GetMapping("/search/alpaca")
//    public ResponseEntity<List<AssetResponse>> searchAlpaca(@RequestParam String query) {
//        System.out.println("Searching");
//        List<Asset> results = assetService.searchFromAlpaca(query);
//        return ResponseEntity.ok(results.stream().map(this::toResponse).toList());
//    }

    @GetMapping("/test-alpaca")
    public ResponseEntity<?> testAlpaca() {
        JsonNode result = alpacaClient.getLatestPrice("AAPL");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/steam")
    public ResponseEntity<?> searchSteam(@RequestParam String query,
                                         @RequestParam(defaultValue = "0") int page) {
        Map<String, Object> searchResult = assetService.searchFromSteam(query, page);
        List<Asset> assets = (List<Asset>) searchResult.get("results");
        int totalCount = (int) searchResult.get("totalCount");

        Map<String, Object> response = new HashMap<>();
        response.put("results", assets.stream().map(this::toResponse).toList());
        response.put("totalCount", totalCount);
        response.put("page", page);
        response.put("totalPages", (int) Math.ceil((double) totalCount / 20));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/price/steam")
    public ResponseEntity<?> getSteamPrice(@RequestParam String name) {
        try {
            JsonNode priceData = steamMarketClient.getPriceOverview(name);
            if (priceData == null || !priceData.path("success").asBoolean()) {
                return ResponseEntity.ok(Map.of("lowestAsk", (Object) null, "lastSale", (Object) null, "volume", (Object) null));
            }
            return ResponseEntity.ok(Map.of(
                    "lowestAsk", steamMarketClient.parsePrice(priceData.path("lowest_price").asText(null)),
                    "lastSale", steamMarketClient.parsePrice(priceData.path("median_price").asText(null)),
                    "volume", steamMarketClient.parseVolume(priceData.path("volume").asText(null))
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("lowestAsk", (Object) null, "lastSale", (Object) null, "volume", (Object) null));
        }
    }
}
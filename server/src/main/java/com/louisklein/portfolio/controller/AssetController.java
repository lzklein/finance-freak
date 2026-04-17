package com.louisklein.portfolio.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.louisklein.portfolio.dto.AssetRequest;
import com.louisklein.portfolio.dto.AssetResponse;
import com.louisklein.portfolio.model.Asset;
import com.louisklein.portfolio.model.PriceCache;
import com.louisklein.portfolio.repository.PriceCacheRepository;
import com.louisklein.portfolio.service.AlpacaClient;
import com.louisklein.portfolio.service.AssetService;
import com.louisklein.portfolio.service.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;
    private final PriceCacheRepository priceCacheRepository;
    private final AlpacaClient alpacaClient;

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

    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<AssetResponse> getAssetBySymbol(@PathVariable String symbol) {
        Asset asset = assetService.findBySymbol(symbol);
        return ResponseEntity.ok(toResponse(asset));
    }

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
                request.getSymbol(),
                request.getName(),
                request.getAssetType(),
                request.getExchange()
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
        Result<Asset> result = assetService.updateAsset(id, request.getName(), request.getExchange());

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
        response.setSymbol(asset.getSymbol());
        response.setName(asset.getName());
        response.setAssetType(asset.getAssetType());
        response.setExchange(asset.getExchange());
        response.setCreatedAt(asset.getCreatedAt());

        Optional<PriceCache> price = priceCacheRepository.findByAssetId(asset.getId());
        price.ifPresent(p -> {
            response.setCurrentPrice(p.getPrice());
            response.setChangePct24h(p.getChangePct24h());
        });

        return response;
    }

    @GetMapping("/search/alpaca")
    public ResponseEntity<List<AssetResponse>> searchAlpaca(@RequestParam String query) {
        List<Asset> results = assetService.searchFromAlpaca(query);
        return ResponseEntity.ok(results.stream().map(this::toResponse).toList());
    }

    @GetMapping("/test-alpaca")
    public ResponseEntity<?> testAlpaca() {
        JsonNode result = alpacaClient.getLatestPrice("AAPL");
        return ResponseEntity.ok(result);
    }
}
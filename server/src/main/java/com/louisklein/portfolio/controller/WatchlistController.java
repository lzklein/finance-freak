package com.louisklein.portfolio.controller;

import com.louisklein.portfolio.dto.*;
import com.louisklein.portfolio.model.*;
import com.louisklein.portfolio.repository.PriceCacheRepository;
import com.louisklein.portfolio.service.AssetService;
import com.louisklein.portfolio.service.Result;
import com.louisklein.portfolio.service.UserService;
import com.louisklein.portfolio.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/watchlists")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final UserService userService;
    private final AssetService assetService;
    private final PriceCacheRepository priceCacheRepository;
    @GetMapping
    public ResponseEntity<List<WatchlistResponse>> getWatchlists(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        List<Watchlist> watchlists = watchlistService.findByUserId(user.getId());
        return ResponseEntity.ok(watchlists.stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WatchlistResponse> getWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        Watchlist watchlist = watchlistService.findById(id);
        return ResponseEntity.ok(toResponse(watchlist));
    }

    @PostMapping
    public ResponseEntity<?> createWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody WatchlistRequest request) {
        User user = userService.findByEmail(userDetails.getUsername());
        Result<Watchlist> result = watchlistService.createWatchlist(user.getId(), request.getName());

        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(result.getMessages());
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(result.getPayload()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> renameWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @RequestBody WatchlistRequest request) {
        User user = userService.findByEmail(userDetails.getUsername());
        Result<Watchlist> result = watchlistService.renameWatchlist(id, user.getId(), request.getName());

        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(result.getMessages());
        }

        return ResponseEntity.ok(toResponse(result.getPayload()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        User user = userService.findByEmail(userDetails.getUsername());
        Result<Void> result = watchlistService.deleteWatchlist(id, user.getId());

        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(result.getMessages());
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/assets")
    public ResponseEntity<?> addAsset(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @RequestBody UUID assetId) {
        User user = userService.findByEmail(userDetails.getUsername());
        Result<WatchlistAsset> result = watchlistService.addAsset(id, assetId, user.getId());

        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(result.getMessages());
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/assets/{assetId}")
    public ResponseEntity<?> removeAsset(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @PathVariable UUID assetId) {
        User user = userService.findByEmail(userDetails.getUsername());
        Result<Void> result = watchlistService.removeAsset(id, assetId, user.getId());

        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(result.getMessages());
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/assets/steam")
    public ResponseEntity<?> addSteamAsset(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @RequestBody AssetRequest request) {

        User user = userService.findByEmail(userDetails.getUsername());

        Result<Asset> assetResult = assetService.findOrCreateAsset(
                request.getName(), request.getAssetType(), request.getImageUrl());

        if (!assetResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(assetResult.getMessages());
        }

        Result<WatchlistAsset> result = watchlistService.addAsset(
                id, assetResult.getPayload().getId(), user.getId());

        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(result.getMessages());
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/top-performers")
    public ResponseEntity<List<AssetResponse>> getTopPerformers(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        List<AssetResponse> performers = watchlistService.getTopPerformers(user.getId());
        return ResponseEntity.ok(performers);
    }

    private WatchlistResponse toResponse(Watchlist watchlist) {
        WatchlistResponse response = new WatchlistResponse();
        response.setId(watchlist.getId());
        response.setName(watchlist.getName());
        response.setCreatedAt(watchlist.getCreatedAt());
        return response;
    }

    @GetMapping("/{id}/assets")
    public ResponseEntity<List<AssetResponse>> getWatchlistAssets(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        List<WatchlistAsset> assets = watchlistService.getAssets(id);
        return ResponseEntity.ok(assets.stream().map(wa -> {
            AssetResponse response = new AssetResponse();
            response.setId(wa.getAsset().getId());
            response.setName(wa.getAsset().getName());
            response.setAssetType(wa.getAsset().getAssetType());
            response.setImageUrl(wa.getAsset().getImageUrl());

            List<PriceCache> prices = priceCacheRepository.findAllByAssetId(wa.getAsset().getId());
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
        }).toList());
    }
}
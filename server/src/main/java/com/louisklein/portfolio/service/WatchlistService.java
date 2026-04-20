package com.louisklein.portfolio.service;

import com.louisklein.portfolio.dto.AssetResponse;
import com.louisklein.portfolio.dto.PriceResponse;
import com.louisklein.portfolio.exception.ResourceNotFoundException;
import com.louisklein.portfolio.model.*;
import com.louisklein.portfolio.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final WatchlistAssetRepository watchlistAssetRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final PriceCacheRepository priceCacheRepository;

    public List<Watchlist> findByUserId(UUID userId) {
        return watchlistRepository.findByUserId(userId);
    }

    public Watchlist findById(UUID id) {
        return watchlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Watchlist not found"));
    }

    public Result<Watchlist> createWatchlist(UUID userId, String name) {
        Result<Watchlist> result = new Result<>();

        if (UserService.isNullOrBlank(name)) {
            result.addMessage("Watchlist name is required", ResultType.INVALID);
            return result;
        }

        if (name.length() > 100) {
            result.addMessage("Watchlist name must be 100 characters or less", ResultType.INVALID);
            return result;
        }

        if (watchlistRepository.existsByUserIdAndName(userId, name.trim())) {
            result.addMessage("You already have a watchlist with that name", ResultType.INVALID);
            return result;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Watchlist watchlist = Watchlist.builder()
                .user(user)
                .name(name.trim())
                .build();

        result.setPayload(watchlistRepository.save(watchlist));
        return result;
    }

    public Result<Watchlist> renameWatchlist(UUID watchlistId, UUID userId, String newName) {
        Result<Watchlist> result = new Result<>();

        if (UserService.isNullOrBlank(newName)) {
            result.addMessage("Watchlist name is required", ResultType.INVALID);
            return result;
        }

        if (newName.length() > 100) {
            result.addMessage("Watchlist name must be 100 characters or less", ResultType.INVALID);
            return result;
        }

        Watchlist watchlist = watchlistRepository.findById(watchlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Watchlist not found"));

        if (!watchlist.getUser().getId().equals(userId)) {
            result.addMessage("You do not have permission to rename this watchlist", ResultType.INVALID);
            return result;
        }

        if (watchlistRepository.existsByUserIdAndName(userId, newName.trim())) {
            result.addMessage("You already have a watchlist with that name", ResultType.INVALID);
            return result;
        }

        watchlist.setName(newName.trim());
        result.setPayload(watchlistRepository.save(watchlist));
        return result;
    }

    public Result<Void> deleteWatchlist(UUID watchlistId, UUID userId) {
        Result<Void> result = new Result<>();

        Watchlist watchlist = watchlistRepository.findById(watchlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Watchlist not found"));

        if (!watchlist.getUser().getId().equals(userId)) {
            result.addMessage("You do not have permission to delete this watchlist", ResultType.INVALID);
            return result;
        }

        watchlistRepository.deleteById(watchlistId);
        return result;
    }

    public Result<WatchlistAsset> addAsset(UUID watchlistId, UUID assetId, UUID userId) {
        Result<WatchlistAsset> result = new Result<>();

        Watchlist watchlist = watchlistRepository.findById(watchlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Watchlist not found"));

        if (!watchlist.getUser().getId().equals(userId)) {
            result.addMessage("You do not have permission to modify this watchlist", ResultType.INVALID);
            return result;
        }

        if (watchlistAssetRepository.existsByWatchlistIdAndAssetId(watchlistId, assetId)) {
            result.addMessage("Asset is already in this watchlist", ResultType.INVALID);
            return result;
        }

        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

        Optional<PriceCache> priceCache = priceCacheRepository
                .findByAssetIdAndMarketplace(asset.getId(), PriceCache.Marketplace.STEAM);

        WatchlistAsset watchlistAsset = WatchlistAsset.builder()
                .watchlist(watchlist)
                .asset(asset)
                .entryPrice(priceCache.map(PriceCache::getLowestAsk).orElse(null))
                .build();

        result.setPayload(watchlistAssetRepository.save(watchlistAsset));
        return result;
    }

    public List<AssetResponse> getTopPerformers(UUID userId) {
        List<Watchlist> watchlists = watchlistRepository.findByUserId(userId);

        return watchlists.stream()
                .flatMap(w -> watchlistAssetRepository.findByWatchlistId(w.getId()).stream())
                .filter(wa -> wa.getEntryPrice() != null)
                .filter(wa -> {
                    Optional<PriceCache> pc = priceCacheRepository
                            .findByAssetIdAndMarketplace(wa.getAsset().getId(), PriceCache.Marketplace.STEAM);
                    return pc.isPresent() && pc.get().getLowestAsk() != null;
                })
                .sorted((a, b) -> {
                    BigDecimal aChange = getPriceChangePct(a);
                    BigDecimal bChange = getPriceChangePct(b);
                    return bChange.compareTo(aChange);
                })
                .limit(5)
                .map(wa -> {
                    Asset asset = wa.getAsset();
                    AssetResponse response = new AssetResponse();
                    response.setId(asset.getId());
                    response.setName(asset.getName());
                    response.setAssetType(asset.getAssetType());
                    response.setImageUrl(asset.getImageUrl());

                    priceCacheRepository.findByAssetIdAndMarketplace(
                                    asset.getId(), PriceCache.Marketplace.STEAM)
                            .ifPresent(pc -> {
                                Map<String, PriceResponse> prices = new HashMap<>();
                                PriceResponse pr = new PriceResponse();
                                pr.setLowestAsk(pc.getLowestAsk());
                                pr.setLastSale(pc.getLastSale());
                                pr.setVolume24h(pc.getVolume24h());
                                pr.setFetchedAt(pc.getFetchedAt());
                                prices.put("STEAM", pr);
                                response.setPrices(prices);
                            });

                    BigDecimal changePct = getPriceChangePct(wa);
                    response.setChangePct(changePct);
                    return response;
                })
                .toList();
    }

    private BigDecimal getPriceChangePct(WatchlistAsset wa) {
        Optional<PriceCache> pc = priceCacheRepository
                .findByAssetIdAndMarketplace(wa.getAsset().getId(), PriceCache.Marketplace.STEAM);
        if (pc.isEmpty() || pc.get().getLowestAsk() == null || wa.getEntryPrice() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal current = pc.get().getLowestAsk();
        BigDecimal entry = wa.getEntryPrice();
        return current.subtract(entry)
                .divide(entry, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    public Result<Void> removeAsset(UUID watchlistId, UUID assetId, UUID userId) {
        Result<Void> result = new Result<>();

        Watchlist watchlist = watchlistRepository.findById(watchlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Watchlist not found"));

        if (!watchlist.getUser().getId().equals(userId)) {
            result.addMessage("You do not have permission to modify this watchlist", ResultType.INVALID);
            return result;
        }

        if (!watchlistAssetRepository.existsByWatchlistIdAndAssetId(watchlistId, assetId)) {
            result.addMessage("Asset is not in this watchlist", ResultType.NOT_FOUND);
            return result;
        }

        watchlistAssetRepository.deleteByWatchlistIdAndAssetId(watchlistId, assetId);
        return result;
    }



    public List<WatchlistAsset> getAssets(UUID watchlistId) {
        return watchlistAssetRepository.findByWatchlistId(watchlistId);
    }
}
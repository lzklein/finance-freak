package com.louisklein.portfolio.service;

import com.louisklein.portfolio.exception.ResourceNotFoundException;
import com.louisklein.portfolio.model.Asset;
import com.louisklein.portfolio.model.User;
import com.louisklein.portfolio.model.Watchlist;
import com.louisklein.portfolio.model.WatchlistAsset;
import com.louisklein.portfolio.repository.AssetRepository;
import com.louisklein.portfolio.repository.UserRepository;
import com.louisklein.portfolio.repository.WatchlistAssetRepository;
import com.louisklein.portfolio.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final WatchlistAssetRepository watchlistAssetRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;

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

        WatchlistAsset watchlistAsset = WatchlistAsset.builder()
                .watchlist(watchlist)
                .asset(asset)
                .build();

        result.setPayload(watchlistAssetRepository.save(watchlistAsset));
        return result;
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
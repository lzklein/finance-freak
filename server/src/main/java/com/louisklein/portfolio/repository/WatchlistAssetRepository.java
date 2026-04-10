package com.louisklein.portfolio.repository;

import com.louisklein.portfolio.model.WatchlistAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WatchlistAssetRepository extends JpaRepository<WatchlistAsset, UUID> {

    List<WatchlistAsset> findByWatchlistId(UUID watchlistId);

    boolean existsByWatchlistIdAndAssetId(UUID watchlistId, UUID assetId);

    void deleteByWatchlistIdAndAssetId(UUID watchlistId, UUID assetId);
}
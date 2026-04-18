package com.louisklein.portfolio.repository;

import com.louisklein.portfolio.model.PriceCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.List;
import java.util.UUID;

@Repository
public interface PriceCacheRepository extends JpaRepository<PriceCache, UUID> {

    Optional<PriceCache> findByAssetId(UUID assetId);
    boolean existsByAssetId(UUID assetId);

    Optional<PriceCache> findByAssetIdAndMarketplace(UUID assetId, PriceCache.Marketplace marketplace);
    List<PriceCache> findAllByAssetId(UUID assetId);
}
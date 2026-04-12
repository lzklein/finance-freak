package com.louisklein.portfolio.repository;

import com.louisklein.portfolio.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID> {

    Optional<Asset> findBySymbol(String symbol);
    boolean existsBySymbol(String symbol);

    Optional<Asset> findByName(String name);
    boolean existsByName(String name);

    List<Asset> findByAssetType(Asset.AssetType assetType);

    List<Asset> findByNameContaining(String name);

    @Query("SELECT a FROM Asset a JOIN FETCH a.priceCache WHERE a.id = :id")
    Optional<Asset> findByIdWithPrice(@Param("id") UUID id);
}
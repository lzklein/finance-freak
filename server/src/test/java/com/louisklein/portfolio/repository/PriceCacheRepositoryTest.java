package com.louisklein.portfolio.repository;

import com.louisklein.portfolio.model.Asset;
import com.louisklein.portfolio.model.PriceCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PriceCacheRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PriceCacheRepository priceCacheRepository;

    private Asset testAsset;
    private PriceCache testPriceCache;

    @BeforeEach
    void setUp() {
        testAsset = Asset.builder()
                .name("AK-47 | Redline (Field-Tested)")
                .assetType(Asset.AssetType.CS2_SKIN)
                .build();
        entityManager.persistAndFlush(testAsset);

        testPriceCache = PriceCache.builder()
                .asset(testAsset)
                .marketplace(PriceCache.Marketplace.STEAM)
                .lowestAsk(new BigDecimal("45.00"))
                .lastSale(new BigDecimal("44.00"))
                .volume24h(150)
                .fetchedAt(OffsetDateTime.now())
                .build();
        entityManager.persistAndFlush(testPriceCache);
    }

    @Test
    void findByAssetIdAndMarketplace_existing_returnsPrice() {
        Optional<PriceCache> result = priceCacheRepository
                .findByAssetIdAndMarketplace(testAsset.getId(), PriceCache.Marketplace.STEAM);

        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("45.00"), result.get().getLowestAsk());
    }

    @Test
    void findByAssetIdAndMarketplace_wrongMarketplace_returnsEmpty() {
        Optional<PriceCache> result = priceCacheRepository
                .findByAssetIdAndMarketplace(testAsset.getId(), PriceCache.Marketplace.SKINPORT);

        assertFalse(result.isPresent());
    }

    @Test
    void findAllByAssetId_existingAsset_returnsPrices() {
        List<PriceCache> results = priceCacheRepository.findAllByAssetId(testAsset.getId());

        assertEquals(1, results.size());
        assertEquals(PriceCache.Marketplace.STEAM, results.get(0).getMarketplace());
    }

    @Test
    void findAllByAssetId_noResults_returnsEmpty() {
        Asset otherAsset = Asset.builder()
                .name("AWP | Dragon Lore (Factory New)")
                .assetType(Asset.AssetType.CS2_SKIN)
                .build();
        entityManager.persistAndFlush(otherAsset);

        List<PriceCache> results = priceCacheRepository.findAllByAssetId(otherAsset.getId());

        assertTrue(results.isEmpty());
    }
}
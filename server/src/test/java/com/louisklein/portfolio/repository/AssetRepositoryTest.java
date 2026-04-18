package com.louisklein.portfolio.repository;

import com.louisklein.portfolio.model.Asset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AssetRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AssetRepository assetRepository;

    private Asset testAsset;

    @BeforeEach
    void setUp() {
        testAsset = Asset.builder()
                .name("AK-47 | Redline (Field-Tested)")
                .assetType(Asset.AssetType.CS2_SKIN)
                .build();
        entityManager.persistAndFlush(testAsset);
    }

    @Test
    void findByName_existingName_returnsAsset() {
        Optional<Asset> result = assetRepository.findByName("AK-47 | Redline (Field-Tested)");

        assertTrue(result.isPresent());
        assertEquals(Asset.AssetType.CS2_SKIN, result.get().getAssetType());
    }

    @Test
    void findByName_nonExistentName_returnsEmpty() {
        Optional<Asset> result = assetRepository.findByName("AWP | Dragon Lore (Factory New)");

        assertFalse(result.isPresent());
    }

    @Test
    void existsByName_existingName_returnsTrue() {
        assertTrue(assetRepository.existsByName("AK-47 | Redline (Field-Tested)"));
    }

    @Test
    void existsByName_nonExistentName_returnsFalse() {
        assertFalse(assetRepository.existsByName("AWP | Dragon Lore (Factory New)"));
    }

    @Test
    void findByNameContaining_partialMatch_returnsAssets() {
        List<Asset> results = assetRepository.findByNameContaining("AK-47");

        assertEquals(1, results.size());
        assertTrue(results.get(0).getName().contains("AK-47"));
    }

    @Test
    void findByNameContaining_noMatch_returnsEmpty() {
        List<Asset> results = assetRepository.findByNameContaining("AWP");

        assertTrue(results.isEmpty());
    }

    @Test
    void findByAssetType_cs2Skin_returnsMatchingAssets() {
        Asset stockAsset = Asset.builder()
                .name("Apple Inc.")
                .assetType(Asset.AssetType.STOCK)
                .build();
        entityManager.persistAndFlush(stockAsset);

        List<Asset> results = assetRepository.findByAssetType(Asset.AssetType.CS2_SKIN);

        assertEquals(1, results.size());
        assertEquals(Asset.AssetType.CS2_SKIN, results.get(0).getAssetType());
    }
}
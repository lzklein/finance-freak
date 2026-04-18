package com.louisklein.portfolio.service;

import com.louisklein.portfolio.exception.ResourceNotFoundException;
import com.louisklein.portfolio.model.Asset;
import com.louisklein.portfolio.repository.AssetRepository;
import com.louisklein.portfolio.repository.PriceCacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock private AssetRepository assetRepository;
    @Mock private PriceCacheRepository priceCacheRepository;
    @Mock private AlpacaClient alpacaClient;

    @InjectMocks
    private AssetService assetService;

    private Asset testAsset;
    private UUID assetId;

    @BeforeEach
    void setUp() {
        assetId = UUID.randomUUID();

        testAsset = Asset.builder()
                .id(assetId)
                .name("AK-47 | Redline (Field-Tested)")
                .assetType(Asset.AssetType.CS2_SKIN)
                .build();
    }

    // findById tests
    @Test
    void findById_existingAsset_returnsAsset() {
        when(assetRepository.findById(any(UUID.class))).thenReturn(Optional.of(testAsset));

        Asset result = assetService.findById(assetId);

        assertNotNull(result);
        assertEquals("AK-47 | Redline (Field-Tested)", result.getName());
    }

    @Test
    void findById_notFound_throwsException() {
        when(assetRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> assetService.findById(UUID.randomUUID()));
    }

    // findByName tests
    @Test
    void findByName_returnsMatchingAssets() {
        when(assetRepository.findByNameContaining(anyString())).thenReturn(List.of(testAsset));

        List<Asset> results = assetService.findByName("AK-47");

        assertEquals(1, results.size());
        assertEquals("AK-47 | Redline (Field-Tested)", results.get(0).getName());
    }

    // findByAssetType tests
    @Test
    void findByAssetType_returnsMatchingAssets() {
        when(assetRepository.findByAssetType(any(Asset.AssetType.class))).thenReturn(List.of(testAsset));

        List<Asset> results = assetService.findByAssetType(Asset.AssetType.CS2_SKIN);

        assertEquals(1, results.size());
        assertEquals(Asset.AssetType.CS2_SKIN, results.get(0).getAssetType());
    }

    // createAsset tests
    @Test
    void createAsset_validInput_returnsSuccess() {
        when(assetRepository.existsByName(anyString())).thenReturn(false);
        when(assetRepository.save(any(Asset.class))).thenReturn(testAsset);

        Result<Asset> result = assetService.createAsset(
                "AK-47 | Redline (Field-Tested)", Asset.AssetType.CS2_SKIN, null);

        assertTrue(result.isSuccess());
        assertNotNull(result.getPayload());
        verify(assetRepository, times(1)).save(any(Asset.class));
    }

    @Test
    void createAsset_blankName_returnsInvalid() {
        Result<Asset> result = assetService.createAsset("", Asset.AssetType.CS2_SKIN, null);

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
        verify(assetRepository, never()).save(any(Asset.class));
    }

    @Test
    void createAsset_nullAssetType_returnsInvalid() {
        Result<Asset> result = assetService.createAsset("AK-47 | Redline (Field-Tested)", null, null);

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
        verify(assetRepository, never()).save(any(Asset.class));
    }

    @Test
    void createAsset_duplicateName_returnsInvalid() {
        when(assetRepository.existsByName(anyString())).thenReturn(true);

        Result<Asset> result = assetService.createAsset(
                "AK-47 | Redline (Field-Tested)", Asset.AssetType.CS2_SKIN, null);

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
        verify(assetRepository, never()).save(any(Asset.class));
    }

    // updateAsset tests
    @Test
    void updateAsset_validInput_returnsSuccess() {
        when(assetRepository.findById(any(UUID.class))).thenReturn(Optional.of(testAsset));
        when(assetRepository.save(any(Asset.class))).thenReturn(testAsset);

        Result<Asset> result = assetService.updateAsset(assetId, "AK-47 | Redline (Minimal Wear)", null);

        assertTrue(result.isSuccess());
        verify(assetRepository, times(1)).save(any(Asset.class));
    }

    @Test
    void updateAsset_blankName_returnsInvalid() {
        Result<Asset> result = assetService.updateAsset(assetId, "", null);

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
        verify(assetRepository, never()).save(any(Asset.class));
    }

    @Test
    void updateAsset_notFound_throwsException() {
        when(assetRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> assetService.updateAsset(UUID.randomUUID(), "New Name", null));
    }

    // deleteAsset tests
    @Test
    void deleteAsset_existingAsset_returnsSuccess() {
        when(assetRepository.existsById(any(UUID.class))).thenReturn(true);
        doNothing().when(assetRepository).deleteById(any(UUID.class));

        Result<Void> result = assetService.deleteAsset(assetId);

        assertTrue(result.isSuccess());
        verify(assetRepository, times(1)).deleteById(assetId);
    }

    @Test
    void deleteAsset_notFound_returnsNotFound() {
        when(assetRepository.existsById(any(UUID.class))).thenReturn(false);

        Result<Void> result = assetService.deleteAsset(UUID.randomUUID());

        assertFalse(result.isSuccess());
        assertEquals(ResultType.NOT_FOUND, result.getType());
        verify(assetRepository, never()).deleteById(any(UUID.class));
    }
}
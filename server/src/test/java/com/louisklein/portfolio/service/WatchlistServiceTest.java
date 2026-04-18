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
class WatchlistServiceTest {

    @Mock private WatchlistRepository watchlistRepository;
    @Mock private WatchlistAssetRepository watchlistAssetRepository;
    @Mock private UserRepository userRepository;
    @Mock private AssetRepository assetRepository;

    @InjectMocks
    private WatchlistService watchlistService;

    private User testUser;
    private Watchlist testWatchlist;
    private Asset testAsset;
    private UUID userId;
    private UUID watchlistId;
    private UUID assetId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        watchlistId = UUID.randomUUID();
        assetId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .username("testuser")
                .build();

        testWatchlist = Watchlist.builder()
                .id(watchlistId)
                .user(testUser)
                .name("My Watchlist")
                .build();

        testAsset = Asset.builder()
                .id(assetId)
                .name("AK-47 | Redline (Field-Tested)")
                .assetType(Asset.AssetType.CS2_SKIN)
                .build();
    }

    // createWatchlist tests
    @Test
    void createWatchlist_validInput_returnsSuccess() {
        when(watchlistRepository.existsByUserIdAndName(any(UUID.class), anyString())).thenReturn(false);
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(watchlistRepository.save(any(Watchlist.class))).thenReturn(testWatchlist);

        Result<Watchlist> result = watchlistService.createWatchlist(userId, "My Watchlist");

        assertTrue(result.isSuccess());
        assertNotNull(result.getPayload());
        verify(watchlistRepository, times(1)).save(any(Watchlist.class));
    }

    @Test
    void createWatchlist_blankName_returnsInvalid() {
        Result<Watchlist> result = watchlistService.createWatchlist(userId, "");

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
        verify(watchlistRepository, never()).save(any(Watchlist.class));
    }

    @Test
    void createWatchlist_duplicateName_returnsInvalid() {
        when(watchlistRepository.existsByUserIdAndName(any(UUID.class), anyString())).thenReturn(true);

        Result<Watchlist> result = watchlistService.createWatchlist(userId, "My Watchlist");

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
        verify(watchlistRepository, never()).save(any(Watchlist.class));
    }

    @Test
    void createWatchlist_nameTooLong_returnsInvalid() {
        String longName = "a".repeat(101);

        Result<Watchlist> result = watchlistService.createWatchlist(userId, longName);

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
    }

    // findByUserId tests
    @Test
    void findByUserId_returnsWatchlists() {
        when(watchlistRepository.findByUserId(any(UUID.class))).thenReturn(List.of(testWatchlist));

        List<Watchlist> results = watchlistService.findByUserId(userId);

        assertEquals(1, results.size());
        assertEquals("My Watchlist", results.get(0).getName());
    }

    // deleteWatchlist tests
    @Test
    void deleteWatchlist_ownerDeletes_returnsSuccess() {
        when(watchlistRepository.findById(any(UUID.class))).thenReturn(Optional.of(testWatchlist));
        doNothing().when(watchlistRepository).deleteById(any(UUID.class));

        Result<Void> result = watchlistService.deleteWatchlist(watchlistId, userId);

        assertTrue(result.isSuccess());
        verify(watchlistRepository, times(1)).deleteById(watchlistId);
    }

    @Test
    void deleteWatchlist_wrongUser_returnsInvalid() {
        when(watchlistRepository.findById(any(UUID.class))).thenReturn(Optional.of(testWatchlist));

        Result<Void> result = watchlistService.deleteWatchlist(watchlistId, UUID.randomUUID());

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
        verify(watchlistRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void deleteWatchlist_notFound_throwsException() {
        when(watchlistRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> watchlistService.deleteWatchlist(watchlistId, userId));
    }

    // addAsset tests
    @Test
    void addAsset_validInput_returnsSuccess() {
        WatchlistAsset watchlistAsset = WatchlistAsset.builder()
                .watchlist(testWatchlist)
                .asset(testAsset)
                .build();

        when(watchlistRepository.findById(any(UUID.class))).thenReturn(Optional.of(testWatchlist));
        when(watchlistAssetRepository.existsByWatchlistIdAndAssetId(any(UUID.class), any(UUID.class))).thenReturn(false);
        when(assetRepository.findById(any(UUID.class))).thenReturn(Optional.of(testAsset));
        when(watchlistAssetRepository.save(any(WatchlistAsset.class))).thenReturn(watchlistAsset);

        Result<WatchlistAsset> result = watchlistService.addAsset(watchlistId, assetId, userId);

        assertTrue(result.isSuccess());
        verify(watchlistAssetRepository, times(1)).save(any(WatchlistAsset.class));
    }

    @Test
    void addAsset_duplicateAsset_returnsInvalid() {
        when(watchlistRepository.findById(any(UUID.class))).thenReturn(Optional.of(testWatchlist));
        when(watchlistAssetRepository.existsByWatchlistIdAndAssetId(any(UUID.class), any(UUID.class))).thenReturn(true);

        Result<WatchlistAsset> result = watchlistService.addAsset(watchlistId, assetId, userId);

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
        verify(watchlistAssetRepository, never()).save(any(WatchlistAsset.class));
    }

    @Test
    void addAsset_wrongUser_returnsInvalid() {
        when(watchlistRepository.findById(any(UUID.class))).thenReturn(Optional.of(testWatchlist));

        Result<WatchlistAsset> result = watchlistService.addAsset(watchlistId, assetId, UUID.randomUUID());

        assertFalse(result.isSuccess());
        assertEquals(ResultType.INVALID, result.getType());
        verify(watchlistAssetRepository, never()).save(any(WatchlistAsset.class));
    }

    // removeAsset tests
    @Test
    void removeAsset_validInput_returnsSuccess() {
        when(watchlistRepository.findById(any(UUID.class))).thenReturn(Optional.of(testWatchlist));
        when(watchlistAssetRepository.existsByWatchlistIdAndAssetId(any(UUID.class), any(UUID.class))).thenReturn(true);
        doNothing().when(watchlistAssetRepository).deleteByWatchlistIdAndAssetId(any(UUID.class), any(UUID.class));

        Result<Void> result = watchlistService.removeAsset(watchlistId, assetId, userId);

        assertTrue(result.isSuccess());
        verify(watchlistAssetRepository, times(1)).deleteByWatchlistIdAndAssetId(watchlistId, assetId);
    }

    @Test
    void removeAsset_assetNotInWatchlist_returnsNotFound() {
        when(watchlistRepository.findById(any(UUID.class))).thenReturn(Optional.of(testWatchlist));
        when(watchlistAssetRepository.existsByWatchlistIdAndAssetId(any(UUID.class), any(UUID.class))).thenReturn(false);

        Result<Void> result = watchlistService.removeAsset(watchlistId, assetId, userId);

        assertFalse(result.isSuccess());
        assertEquals(ResultType.NOT_FOUND, result.getType());
        verify(watchlistAssetRepository, never()).deleteByWatchlistIdAndAssetId(any(UUID.class), any(UUID.class));
    }
}
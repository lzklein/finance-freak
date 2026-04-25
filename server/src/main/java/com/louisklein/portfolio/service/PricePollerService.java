package com.louisklein.portfolio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.louisklein.portfolio.model.Alert;
import com.louisklein.portfolio.model.Asset;
import com.louisklein.portfolio.model.PriceCache;
import com.louisklein.portfolio.repository.AlertRepository;
import com.louisklein.portfolio.repository.AssetRepository;
import com.louisklein.portfolio.repository.PriceCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricePollerService {

    private final AssetRepository assetRepository;
    private final PriceCacheRepository priceCacheRepository;
    private final AlertRepository alertRepository;
    private final AlertService alertService;
    private final SteamMarketClient steamMarketClient;

    @Scheduled(fixedRate = 60000)
    public void pollPrices() {
        log.info("Starting price poll...");

        List<Asset> cs2Assets = assetRepository.findByAssetType(Asset.AssetType.CS2_SKIN);

        for (Asset asset : cs2Assets) {
            try {
                pollSteamPrice(asset);
                Thread.sleep(1500); // 1.5 seconds between each request
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Failed to poll price for {}: {}", asset.getName(), e.getMessage());
            }
        }

        log.info("Price poll complete.");
    }

    private void pollSteamPrice(Asset asset) {
        JsonNode response = steamMarketClient.getPriceOverview(asset.getName());

        if (response == null || !response.path("success").asBoolean()) {
            log.warn("No price data for {}", asset.getName());
            return;
        }

        BigDecimal lowestAsk = steamMarketClient.parsePrice(
                response.path("lowest_price").asText(null));
        BigDecimal lastSale = steamMarketClient.parsePrice(
                response.path("median_price").asText(null));
        Integer volume = steamMarketClient.parseVolume(
                response.path("volume").asText(null));

        upsertPriceCache(asset, lowestAsk, lastSale, volume);
        checkAlerts(asset, lowestAsk);
    }

    private void upsertPriceCache(Asset asset, BigDecimal lowestAsk,
                                  BigDecimal lastSale, Integer volume) {
        Optional<PriceCache> existing = priceCacheRepository
                .findByAssetIdAndMarketplace(asset.getId(), PriceCache.Marketplace.STEAM);

        PriceCache priceCache = existing.orElse(PriceCache.builder()
                .asset(asset)
                .marketplace(PriceCache.Marketplace.STEAM)
                .build());

        priceCache.setLowestAsk(lowestAsk);
        priceCache.setLastSale(lastSale);
        priceCache.setVolume24h(volume);
        priceCache.setFetchedAt(OffsetDateTime.now());
        priceCacheRepository.save(priceCache);
    }

    private void checkAlerts(Asset asset, BigDecimal currentPrice) {
        if (currentPrice == null) return;

        List<Alert> activeAlerts = alertRepository.findByAssetId(asset.getId())
                .stream()
                .filter(Alert::isActive)
                .filter(a -> a.getMarketplace() == PriceCache.Marketplace.STEAM)
                .toList();

        for (Alert alert : activeAlerts) {
            boolean triggered = false;

            if (alert.getCondition() == Alert.AlertCondition.ABOVE
                    && currentPrice.compareTo(alert.getThreshold()) > 0) {
                triggered = true;
            } else if (alert.getCondition() == Alert.AlertCondition.BELOW
                    && currentPrice.compareTo(alert.getThreshold()) < 0) {
                triggered = true;
            }

            if (triggered) {
                log.info("Alert triggered for {} {} {}",
                        asset.getName(),
                        alert.getCondition(),
                        alert.getThreshold());
                alertService.triggerAlert(alert, currentPrice);
            }
        }
    }

    public void pollNow() {
        pollPrices();
    }
}
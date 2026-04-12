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
    private final AlpacaClient alpacaClient;

    @Scheduled(fixedRate = 300000)
    public void pollPrices() {
        log.info("Starting price poll...");
        List<Asset> assets = assetRepository.findAll();

        for (Asset asset : assets) {
            try {
                BigDecimal price = fetchPrice(asset);
                if (price == null) continue;

                upsertPriceCache(asset, price);
                checkAlerts(asset, price);

            } catch (Exception e) {
                log.error("Failed to poll price for {}: {}", asset.getSymbol(), e.getMessage());
            }
        }

        log.info("Price poll complete.");
    }

    private BigDecimal fetchPrice(Asset asset) {
        try {
            if (asset.getAssetType() == Asset.AssetType.CRYPTO) {
                JsonNode response = alpacaClient.getLatestCryptoPrice(asset.getSymbol());
                JsonNode quote = response.path("quotes").path(asset.getSymbol());
                String askPrice = quote.path("ap").asText();
                return new BigDecimal(askPrice);
            } else {
                JsonNode response = alpacaClient.getLatestPrice(asset.getSymbol());
                JsonNode quote = response.path("quote");
                String askPrice = quote.path("ap").asText();
                return new BigDecimal(askPrice);
            }
        } catch (Exception e) {
            log.warn("Could not fetch price for {}: {}", asset.getSymbol(), e.getMessage());
            return null;
        }
    }

    private void upsertPriceCache(Asset asset, BigDecimal price) {
        Optional<PriceCache> existing = priceCacheRepository.findByAssetId(asset.getId());

        PriceCache priceCache = existing.orElse(PriceCache.builder()
                .asset(asset)
                .build());

        priceCache.setPrice(price);
        priceCache.setFetchedAt(OffsetDateTime.now());
        priceCacheRepository.save(priceCache);
    }

    private void checkAlerts(Asset asset, BigDecimal currentPrice) {
        List<Alert> activeAlerts = alertRepository.findByAssetId(asset.getId())
                .stream()
                .filter(Alert::isActive)
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
                        alert.getAsset().getSymbol(),
                        alert.getCondition(),
                        alert.getThreshold());
                alertService.triggerAlert(alert, currentPrice);
            }
        }
    }
}
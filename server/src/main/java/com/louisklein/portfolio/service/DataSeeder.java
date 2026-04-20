package com.louisklein.portfolio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.louisklein.portfolio.model.Asset;
import com.louisklein.portfolio.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final AssetRepository assetRepository;
    private final SteamMarketClient steamMarketClient;
    private final PricePollerService pricePollerService;

    private static final List<String> POPULAR_SKINS = List.of(
            "AK-47 | Redline (Field-Tested)",
            "AK-47 | Asiimov (Field-Tested)",
            "AWP | Asiimov (Field-Tested)",
            "AWP | Dragon Lore (Factory New)",
            "M4A4 | Howl (Field-Tested)",
            "AK-47 | Fire Serpent (Field-Tested)",
            "AWP | Medusa (Field-Tested)",
            "M4A1-S | Knight (Factory New)",
            "AK-47 | Wild Lotus (Factory New)",
            "Karambit | Fade (Factory New)"
    );

    @Override
    public void run(ApplicationArguments args) {
        log.info("Seeding popular assets...");
        for (String name : POPULAR_SKINS) {
            if (!assetRepository.existsByName(name)) {
                JsonNode response = steamMarketClient.searchItems(name, 0, 1);
                String imageUrl = null;
                if (response != null && response.path("results").isArray()
                        && response.path("results").size() > 0) {
                    imageUrl = "https://community.akamai.steamstatic.com/economy/image/"
                            + response.path("results").get(0)
                            .path("asset_description").path("icon_url").asText();
                }
                Asset asset = Asset.builder()
                        .name(name)
                        .assetType(Asset.AssetType.CS2_SKIN)
                        .imageUrl(imageUrl)
                        .build();
                assetRepository.save(asset);
                log.info("Seeded: {}", name);
            }
        }
        log.info("Triggering initial price poll...");
        pricePollerService.pollNow();

        log.info("Seeding complete.");
    }
}
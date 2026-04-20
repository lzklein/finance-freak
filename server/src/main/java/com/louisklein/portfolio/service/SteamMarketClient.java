package com.louisklein.portfolio.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Component
public class SteamMarketClient {

    private final WebClient webClient;

    public SteamMarketClient() {
        this.webClient = WebClient.builder()
                .baseUrl("https://steamcommunity.com")
                .build();
    }

    @Cacheable(value = "steamPrices", key = "#marketHashName")
    public JsonNode getPriceOverview(String marketHashName) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/market/priceoverview/")
                        .queryParam("appid", "730")
                        .queryParam("currency", "1")
                        .queryParam("market_hash_name", marketHashName)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }
    public BigDecimal parsePrice(String priceStr) {
        if (priceStr == null || priceStr.isBlank()) return null;
        String cleaned = priceStr.replaceAll("[^0-9.]", "");
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Integer parseVolume(String volumeStr) {
        if (volumeStr == null || volumeStr.isBlank()) return null;
        try {
            return Integer.parseInt(volumeStr.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public JsonNode searchItems(String query, int start, int count) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/market/search/render/")
                        .queryParam("query", query)
                        .queryParam("appid", "730")
                        .queryParam("norender", "1")
                        .queryParam("start", start)
                        .queryParam("count", count)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode getPriceHistory(String marketHashName) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/market/pricehistory/")
                            .queryParam("appid", "730")
                            .queryParam("market_hash_name", marketHashName)
                            .build())
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> response.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("Steam error: " + response.statusCode())))
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (Exception e) {
//            log.warn("Price history fetch failed for {}: {}", marketHashName, e.getMessage());
            return null;
        }
    }
}
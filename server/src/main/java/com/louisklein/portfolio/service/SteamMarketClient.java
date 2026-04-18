package com.louisklein.portfolio.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Component
public class SteamMarketClient {

    private final WebClient webClient;

    public SteamMarketClient() {
        this.webClient = WebClient.builder()
                .baseUrl("https://steamcommunity.com/market")
                .build();
    }

    public JsonNode getPriceOverview(String marketHashName) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/priceoverview/")
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
}
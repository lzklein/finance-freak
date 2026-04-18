package com.louisklein.portfolio.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class AlpacaClient {

    private final WebClient webClient;

    @Value("${alpaca.api.key}")
    private String apiKey;

    @Value("${alpaca.api.secret}")
    private String apiSecret;

    public AlpacaClient() {
        this.webClient = WebClient.builder()
                .baseUrl("https://data.alpaca.markets")
                .build();
    }

    private WebClient.RequestHeadersSpec<?> withAuth(WebClient.RequestHeadersSpec<?> request) {
        return request
                .header("APCA-API-KEY-ID", apiKey)
                .header("APCA-API-SECRET-KEY", apiSecret);
    }

    public JsonNode getLatestPrice(String symbol) {
        return withAuth(webClient.get()
                .uri("/v2/stocks/{symbol}/quotes/latest", symbol))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode getLatestCryptoPrice(String symbol) {
        return withAuth(webClient.get()
                .uri("/v1beta3/crypto/us/latest/quotes?symbols={symbol}", symbol))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode searchAssets(String query) {
        return withAuth(webClient.get()
                .uri("https://paper-api.alpaca.markets/v2/assets?search={query}&asset_class=us_equity", query))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode searchCryptoAssets(String query) {
        return withAuth(webClient.get()
                .uri("https://paper-api.alpaca.markets/v2/assets?search={query}&asset_class=crypto", query))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode getAssetBySymbol(String symbol) {
        return withAuth(webClient.get()
                .uri("https://broker-api.alpaca.markets/v1/assets/{symbol}", symbol))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }
}
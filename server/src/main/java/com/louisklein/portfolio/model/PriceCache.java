package com.louisklein.portfolio.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "price_cache")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceCache {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "marketplace", nullable = false, length = 20)
    private Marketplace marketplace;

    @Column(name = "lowest_ask", precision = 18, scale = 2)
    private BigDecimal lowestAsk;

    @Column(name = "highest_bid", precision = 18, scale = 2)
    private BigDecimal highestBid;

    @Column(name = "last_sale", precision = 18, scale = 2)
    private BigDecimal lastSale;

    @Column(name = "volume_24h")
    private Integer volume24h;

    @Column(name = "fetched_at", nullable = false)
    private OffsetDateTime fetchedAt;

    public enum Marketplace {
        STEAM, SKINPORT, BUFF, CSFLOAT, SKINBARON, NASDAQ, NYSE, CRYPTO
    }
}
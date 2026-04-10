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

    @Column(name = "price", nullable = false, precision = 18, scale = 8)
    private BigDecimal price;

    @Column(name = "change_pct_24h", precision = 8, scale = 4)
    private BigDecimal changePct24h;

    @Column(name = "volume")
    private Long volume;

    @Column(name = "fetched_at", nullable = false)
    private OffsetDateTime fetchedAt;
}
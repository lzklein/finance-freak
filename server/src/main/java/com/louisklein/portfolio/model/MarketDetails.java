package com.louisklein.portfolio.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "market_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Column(name = "exchange", length = 50)
    private String exchange;

    @Column(name = "asset_class", length = 20)
    private String assetClass;

}
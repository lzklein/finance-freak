package com.louisklein.portfolio.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "skin_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkinDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "weapon_type", length = 50)
    private String weaponType;

    @Enumerated(EnumType.STRING)
    @Column(name = "rarity", length = 20)
    private Rarity rarity;

    @Enumerated(EnumType.STRING)
    @Column(name = "exterior", length = 20)
    private Exterior exterior;

    @Column(name = "is_stattrak", nullable = false)
    private boolean stattrak;

    @Column(name = "is_souvenir", nullable = false)
    private boolean souvenir;

    @Column(name = "float_min", precision = 10, scale = 8)
    private BigDecimal floatMin;

    @Column(name = "float_max", precision = 10, scale = 8)
    private BigDecimal floatMax;

    public enum Rarity {
        CONSUMER, INDUSTRIAL, MIL_SPEC, RESTRICTED, CLASSIFIED, COVERT, CONTRABAND
    }

    public enum Exterior {
        FN, MW, FT, WW, BS
    }
}
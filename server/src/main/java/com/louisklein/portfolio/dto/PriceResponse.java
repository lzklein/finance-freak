package com.louisklein.portfolio.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class PriceResponse {
    private BigDecimal lowestAsk;
    private BigDecimal highestBid;
    private BigDecimal lastSale;
    private Integer volume24h;
    private OffsetDateTime fetchedAt;
}
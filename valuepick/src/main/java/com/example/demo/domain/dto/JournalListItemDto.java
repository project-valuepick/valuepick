package com.example.demo.domain.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalListItemDto {

    private String type;       // "buy" | "sell" | "position"
    private Long id;
    private Long journalId;
    private String title;
    private String stockCode;
    private String corpName;
    private Boolean isShared;

    // buy
    private LocalDateTime buyAt;

    // sell
    private LocalDateTime sellAt;

    // position
    private String state;
    private LocalDateTime firstBuyAt;
    private LocalDateTime finalSellAt;
    private String note;
    private Integer holdingQty;
    private Long usedAmount;
    private Long soldAmount;
    private Long pnl;
    private Long currentPrice;

    // buy/sell (개별 기록)
    private Long price;
    private Integer quantity;
}

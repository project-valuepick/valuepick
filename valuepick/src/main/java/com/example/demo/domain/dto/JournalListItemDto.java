package com.example.demo.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "투자일지 목록 아이템 (buy/sell/position 공용)")
public class JournalListItemDto {

    @Schema(description = "아이템 종류", example = "position", allowableValues = {"buy", "sell", "position"})
    private String type;
    @Schema(description = "ID (buy/sell 개별 기록 ID 또는 position ID)", example = "1")
    private Long id;
    @Schema(description = "소속 투자일지(포지션) ID", example = "1")
    private Long journalId;
    @Schema(description = "제목", example = "삼성전자 장기투자")
    private String title;
    @Schema(description = "종목코드", example = "005930")
    private String stockCode;
    @Schema(description = "기업명", example = "삼성전자")
    private String corpName;
    @Schema(description = "공유 여부")
    private Boolean isShared;

    @Schema(description = "매수 일시 (type=buy일 때)")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime buyAt;

    @Schema(description = "매도 일시 (type=sell일 때)")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sellAt;

    @Schema(description = "포지션 상태 (type=position일 때)", example = "HOLDING")
    private String state;
    @Schema(description = "최초 매수 일시 (type=position일 때)")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime firstBuyAt;
    @Schema(description = "최종 매도 일시 (type=position일 때)")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime finalSellAt;
    @Schema(description = "메모 (type=position일 때)")
    private String note;
    @Schema(description = "보유 수량 (type=position일 때)")
    private Integer holdingQty;
    @Schema(description = "매수에 사용한 총 금액 (type=position일 때)")
    private Long usedAmount;
    @Schema(description = "매도로 회수한 총 금액 (type=position일 때)")
    private Long soldAmount;
    @Schema(description = "손익 (type=position일 때)")
    private Long pnl;
    @Schema(description = "현재가 (type=position일 때)")
    private Long currentPrice;

    @Schema(description = "단가 (type=buy/sell일 때)")
    private Long price;
    @Schema(description = "수량 (type=buy/sell일 때)")
    private Integer quantity;
}

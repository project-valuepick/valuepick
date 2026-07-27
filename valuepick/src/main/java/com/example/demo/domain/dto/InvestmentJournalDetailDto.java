package com.example.demo.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "투자일지(포지션) 상세 정보")
public class InvestmentJournalDetailDto {

    @Schema(description = "포지션 ID", example = "1")
    private Long id;
    @Schema(description = "투자일지 제목", example = "삼성전자 장기투자")
    private String title;
    @Schema(description = "종목코드", example = "005930")
    private String stockCode;
    @Schema(description = "기업명", example = "삼성전자")
    private String corpName;
    @Schema(description = "포지션 상태", example = "HOLDING")
    private String state;
    @Schema(description = "최초 매수 일시")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime firstBuyAt;
    @Schema(description = "최종 매도 일시 (전량 매도로 종료된 경우)")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime finalSellAt;
    @Schema(description = "메모")
    private String note;
    @Schema(description = "공유 여부")
    private Boolean isShared;
    @Schema(description = "현재 보유 수량")
    private Integer holdingQty;
    @Schema(description = "매수에 사용한 총 금액")
    private Long usedAmount;
    @Schema(description = "매도로 회수한 총 금액")
    private Long soldAmount;
    @Schema(description = "손익")
    private Long pnl;
    @Schema(description = "현재가")
    private Long currentPrice;
    @Schema(description = "매수 기록 목록")
    private List<InvestmentBuyDto> buys;
    @Schema(description = "매도 기록 목록")
    private List<InvestmentSellDto> sells;
}

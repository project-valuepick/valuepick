package com.example.demo.domain.dto;

import com.example.demo.domain.entity.InvestmentSell;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "매도 기록")
public class InvestmentSellDto {

    @Schema(description = "매도 기록 ID", example = "1")
    private Long id;
    @Schema(description = "소속 투자일지(포지션) ID", example = "1")
    private Long positionId;
    @Schema(description = "종목코드", example = "005930")
    private String stockCode;
    @Schema(description = "기업명", example = "삼성전자")
    private String corpName;
    @Schema(description = "매도 일시")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sellAt;
    @Schema(description = "매도 단가", example = "75000")
    private Long price;
    @Schema(description = "매도 수량", example = "5")
    private Integer quantity;
    @Schema(description = "공유 여부")
    private Boolean isShared;
    @Schema(description = "등록 일시")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static InvestmentSellDto from(InvestmentSell entity) {
        return InvestmentSellDto.builder()
                .id(entity.getId())
                .positionId(entity.getPosition().getId())
                .stockCode(entity.getStockCode())
                .corpName(entity.getCorpName())
                .sellAt(entity.getSellAt())
                .price(entity.getPrice())
                .quantity(entity.getQuantity())
                .isShared(entity.getIsShared())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

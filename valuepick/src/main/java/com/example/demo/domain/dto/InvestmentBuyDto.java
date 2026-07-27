package com.example.demo.domain.dto;

import com.example.demo.domain.entity.InvestmentBuy;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "매수 기록")
public class InvestmentBuyDto {

    @Schema(description = "매수 기록 ID", example = "1")
    private Long id;
    @Schema(description = "소속 투자일지(포지션) ID", example = "1")
    private Long positionId;
    @Schema(description = "종목코드", example = "005930")
    private String stockCode;
    @Schema(description = "기업명", example = "삼성전자")
    private String corpName;
    @Schema(description = "매수 일시")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime buyAt;
    @Schema(description = "매수 단가", example = "70000")
    private Long price;
    @Schema(description = "매수 수량", example = "10")
    private Integer quantity;
    @Schema(description = "공유 여부")
    private Boolean isShared;
    @Schema(description = "등록 일시")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static InvestmentBuyDto from(InvestmentBuy entity) {
        return InvestmentBuyDto.builder()
                .id(entity.getId())
                .positionId(entity.getPosition().getId())
                .stockCode(entity.getStockCode())
                .corpName(entity.getCorpName())
                .buyAt(entity.getBuyAt())
                .price(entity.getPrice())
                .quantity(entity.getQuantity())
                .isShared(entity.getIsShared())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

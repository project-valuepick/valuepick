package com.example.demo.domain.dto;

import com.example.demo.domain.entity.InvestmentSell;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentSellDto {

    private Long id;
    private Long positionId;
    private String title;
    private String stockCode;
    private String corpName;
    private LocalDateTime sellAt;
    private Long price;
    private Integer quantity;
    private Boolean isShared;
    private LocalDateTime createdAt;

    public static InvestmentSellDto from(InvestmentSell entity) {
        return InvestmentSellDto.builder()
                .id(entity.getId())
                .positionId(entity.getPosition().getId())
                .title(entity.getTitle())
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

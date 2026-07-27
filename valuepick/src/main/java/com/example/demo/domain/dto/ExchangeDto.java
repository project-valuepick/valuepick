package com.example.demo.domain.dto;

import com.example.demo.domain.entity.Exchange;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "환율 정보")
public class ExchangeDto {

    @Schema(description = "통화 단위", example = "USD")
    private String curUnit;
    @Schema(description = "기준일자", example = "2026-06-21")
    private LocalDate baseDate;
    @Schema(description = "국가명", example = "미국")
    private String country;
    @Schema(description = "매매기준율", example = "1380.5")
    private Double dealBasR;
    @Schema(description = "전일 대비 등락률(%)", example = "0.42")
    private Double changeRate;
    @Schema(description = "전일 대비 등락폭", example = "5.8")
    private Double changeAmount;

    public Exchange toEntity() {
        return Exchange.builder()
                .curUnit(this.curUnit)
                .baseDate(this.baseDate)
                .country(this.country)
                .dealBasR(this.dealBasR)
                .changeRate(this.changeRate)
                .changeAmount(this.changeAmount)
                .build();
    }

    public static ExchangeDto from(Exchange entity) {
        return ExchangeDto.builder()
                .curUnit(entity.getCurUnit())
                .baseDate(entity.getBaseDate())
                .country(entity.getCountry())
                .dealBasR(entity.getDealBasR())
                .changeRate(entity.getChangeRate())
                .changeAmount(entity.getChangeAmount())
                .build();
    }
}

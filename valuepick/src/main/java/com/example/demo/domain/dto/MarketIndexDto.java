package com.example.demo.domain.dto;

import com.example.demo.domain.entity.MarketIndex;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "시장지수(코스피) 정보")
public class MarketIndexDto {

    @Schema(description = "ID", example = "1")
    private Long id;
    @Schema(description = "기준일자", example = "2026-06-13")
    private LocalDate basDd;
    @Schema(description = "지수명", example = "코스피")
    private String idxNm;
    @Schema(description = "전일 대비 등락률(%)", example = "0.35")
    private Double flucRt;
    @Schema(description = "시가지수")
    private Double opnprcIdx;
    @Schema(description = "종가지수")
    private Double clsprcIdx;
    @Schema(description = "전일 대비 등락폭")
    private Double cmpprevddIdx;
    @Schema(description = "시가총액")
    private Long mktcap;

    public MarketIndex toEntity() {
        return MarketIndex.builder()
                .id(this.id)
                .basDd(this.basDd)
                .idxNm(this.idxNm)
                .flucRt(this.flucRt)
                .opnprcIdx(this.opnprcIdx)
                .clsprcIdx(this.clsprcIdx)
                .cmpprevddIdx(this.cmpprevddIdx)
                .mktcap(this.mktcap)
                .build();
    }

    public static MarketIndexDto from(MarketIndex entity) {
        return MarketIndexDto.builder()
                .id(entity.getId())
                .basDd(entity.getBasDd())
                .idxNm(entity.getIdxNm())
                .flucRt(entity.getFlucRt())
                .opnprcIdx(entity.getOpnprcIdx())
                .clsprcIdx(entity.getClsprcIdx())
                .cmpprevddIdx(entity.getCmpprevddIdx())
                .mktcap(entity.getMktcap())
                .build();
    }
}

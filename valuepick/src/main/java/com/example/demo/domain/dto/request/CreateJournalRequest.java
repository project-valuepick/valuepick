package com.example.demo.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "투자일지 생성 요청")
public class CreateJournalRequest {

    @Schema(description = "투자일지 제목", example = "삼성전자 장기투자")
    private String title;
    @Schema(description = "종목코드", example = "005930")
    private String stockCode;
    @Schema(description = "기업명", example = "삼성전자")
    private String corpName;
    @Schema(description = "최초 매수 일시")
    private LocalDateTime buyAt;
    @Schema(description = "매수 단가", example = "70000")
    private Long price;
    @Schema(description = "매수 수량", example = "10")
    private Integer quantity;
}

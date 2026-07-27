package com.example.demo.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매도 기록 추가 요청")
public class AddSellRequest {

    @Schema(description = "매도 일시")
    private LocalDateTime sellAt;
    @Schema(description = "매도 단가", example = "75000")
    private Long price;
    @Schema(description = "매도 수량", example = "5")
    private Integer quantity;
}

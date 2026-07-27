package com.example.demo.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매수 기록 추가 요청")
public class AddBuyRequest {

    @Schema(description = "매수 일시")
    private LocalDateTime buyAt;
    @Schema(description = "매수 단가", example = "70000")
    private Long price;
    @Schema(description = "매수 수량", example = "10")
    private Integer quantity;
}

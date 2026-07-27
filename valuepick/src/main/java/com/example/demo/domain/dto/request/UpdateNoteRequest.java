package com.example.demo.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "메모 수정 요청")
public class UpdateNoteRequest {

    @Schema(description = "변경할 메모 내용", example = "실적 발표 이후 추가 매수 검토")
    private String note;
}

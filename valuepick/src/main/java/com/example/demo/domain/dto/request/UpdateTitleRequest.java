package com.example.demo.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "제목 수정 요청")
public class UpdateTitleRequest {

    @Schema(description = "변경할 제목", example = "삼성전자 장기투자")
    private String title;
}

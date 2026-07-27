package com.example.demo.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "투자일지 목록 페이징 결과")
public class JournalPageResult {

    @Schema(description = "현재 페이지의 투자일지 목록")
    private List<JournalListItemDto> content;
    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    private int page;
    @Schema(description = "전체 페이지 수", example = "5")
    private int totalPages;
    @Schema(description = "전체 항목 수", example = "42")
    private long totalElements;
}

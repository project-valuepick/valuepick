package com.example.demo.domain.dto;

import com.example.demo.domain.entity.News;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "종목 관련 뉴스")
public class NewsDto {

    @Schema(description = "뉴스 제목")
    private String title;
    @Schema(description = "언론사", example = "연합뉴스")
    private String press;
    @Schema(description = "뉴스 링크 URL")
    private String link;
    @Schema(description = "게시 일시")
    private LocalDateTime publishedAt;

    public static NewsDto from(News entity) {
        return NewsDto.builder()
                .title(entity.getTitle())
                .press(entity.getPress())
                .link(entity.getLink())
                .publishedAt(entity.getPublishedAt())
                .build();
    }
}

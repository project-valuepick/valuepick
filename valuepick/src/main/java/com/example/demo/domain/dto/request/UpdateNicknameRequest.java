package com.example.demo.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "닉네임 수정 요청")
public class UpdateNicknameRequest {

    @Schema(description = "변경할 닉네임", example = "가치투자자")
    @NotBlank(message = "닉네임은 필수 항목입니다.")
    private String nickname;
}

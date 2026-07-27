package com.example.demo.domain.dto.response;

import com.example.demo.domain.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "회원 정보 응답")
public class UserResponse {

    @Schema(description = "사용자 ID", example = "1")
    private final Long id;
    @Schema(description = "이메일", example = "user@example.com")
    private final String email;
    @Schema(description = "닉네임", example = "가치투자자")
    private final String nickname;

    @Schema(description = "가입일시")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime createdAt;

    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.createdAt = user.getCreatedAt();
    }
}

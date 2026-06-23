package com.example.demo.domain.dto;

import com.example.demo.domain.entity.User;
import com.example.demo.domain.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    @NotBlank(message = "이메일은 필수 항목입니다.")
    @Email(message = "example@example.com 형식으로 입력하세요.")
    private String id;

    @NotBlank(message = "비밀번호는 필수 항목입니다.")
    private String password;

    @NotBlank(message = "닉네임은 필수 항목입니다.")
    private String nickname;

    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User toEntity() {
        return User.builder()
                .id(this.id)
                .password(this.password)
                .nickname(this.nickname)
                .role(this.role)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    public static UserDto from(User entity) {
        return UserDto.builder()
                .id(entity.getId())
                .nickname(entity.getNickname())
                .role(entity.getRole())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

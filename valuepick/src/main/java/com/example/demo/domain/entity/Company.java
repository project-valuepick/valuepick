package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "COMPANY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @Column(name = "stock_code")
    private String stockCode;

    @Column(name = "corp_code", unique = true)
    private String corpCode;

    @Column(name = "corp_name")
    private String corpName;

    @Column(name = "corp_cls", columnDefinition = "CHAR(1)")
    private String corpCls;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

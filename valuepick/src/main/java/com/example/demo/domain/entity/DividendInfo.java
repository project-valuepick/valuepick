package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DIVIDEND_INFO")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DividendInfo {

    @Id
    @Column(name = "corp_code")
    private String corpCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corp_code", referencedColumnName = "corp_code", insertable = false, updatable = false)
    private Company company;

    @Column(name = "dividend_kind")
    private String dividendKind;

    @Column(name = "dividend_amount")
    private Long dividendAmount;

    @Column(name = "stlm_dt")
    private LocalDateTime stlmDt;
}

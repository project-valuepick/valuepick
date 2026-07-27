package com.example.demo.domain.controller;

import com.example.demo.domain.dto.*;
import com.example.demo.domain.dto.request.*;
import com.example.demo.domain.service.InvestmentJournalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "투자일지", description = "투자일지 작성, 매수/매도 기록, 조회, 공유 설정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/journal")
public class InvestmentJournalController {

    private final InvestmentJournalService journalService;

    @Operation(summary = "투자일지 목록 조회", description = "카테고리, 검색어, 기간으로 필터링하여 투자일지 목록을 페이징 조회한다.")
    @GetMapping
    public ResponseEntity<JournalPageResult> getList(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "all") String category,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(
                journalService.getList(userDetails.getUsername(), category, q, from, to, page));
    }

    @Operation(summary = "투자일지 상세 조회", description = "포지션(journalId) 기준으로 투자일지 상세 내역(매수/매도 기록 포함)을 조회한다.")
    @GetMapping("/position/{journalId}")
    public ResponseEntity<InvestmentJournalDetailDto> getDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long journalId) {
        return ResponseEntity.ok(
                journalService.getDetail(userDetails.getUsername(), journalId));
    }

    @Operation(summary = "투자일지 생성", description = "신규 투자일지(포지션)를 생성한다.")
    @PostMapping
    public ResponseEntity<Void> createJournal(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateJournalRequest req) {
        journalService.createJournal(userDetails.getUsername(), req);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "매수 기록 추가", description = "지정한 투자일지(journalId)에 매수 기록을 추가한다.")
    @PostMapping("/{journalId}/buy")
    public ResponseEntity<Void> addBuy(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long journalId,
            @RequestBody AddBuyRequest req) {
        journalService.addBuy(userDetails.getUsername(), journalId, req);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "매도 기록 추가", description = "지정한 투자일지(journalId)에 매도 기록을 추가한다.")
    @PostMapping("/{journalId}/sell")
    public ResponseEntity<Void> addSell(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long journalId,
            @RequestBody AddSellRequest req) {
        journalService.addSell(userDetails.getUsername(), journalId, req);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "제목 수정", description = "투자일지 또는 포지션(type, id)의 제목을 수정한다.")
    @PatchMapping("/{type}/{id}/title")
    public ResponseEntity<Void> updateTitle(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String type,
            @PathVariable Long id,
            @RequestBody UpdateTitleRequest req) {
        journalService.updateTitle(userDetails.getUsername(), type, id, req);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "공유 상태 토글", description = "투자일지 또는 포지션(type, id)의 공유 여부를 전환한다.")
    @PatchMapping("/{type}/{id}/share")
    public ResponseEntity<Void> toggleShare(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String type,
            @PathVariable Long id) {
        journalService.toggleShare(userDetails.getUsername(), type, id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "메모 수정", description = "지정한 포지션(journalId)의 메모를 수정한다.")
    @PatchMapping("/position/{journalId}/note")
    public ResponseEntity<Void> updateNote(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long journalId,
            @RequestBody UpdateNoteRequest req) {
        journalService.updateNote(userDetails.getUsername(), journalId, req);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "투자일지 삭제", description = "투자일지 또는 포지션(type, id)을 삭제한다.")
    @DeleteMapping("/{type}/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String type,
            @PathVariable Long id) {
        journalService.delete(userDetails.getUsername(), type, id);
        return ResponseEntity.ok().build();
    }
}

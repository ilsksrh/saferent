package backend.saferent.controller;

import backend.saferent.dto.request.report.CreateReportRequest;
import backend.saferent.dto.request.report.ResolveReportRequest;
import backend.saferent.dto.response.report.ReportResponse;
import backend.saferent.entity.enums.ReportStatus;
import backend.saferent.service.ReportService;
import backend.saferent.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "Жалобы пользователей")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "Создать жалобу")
    @PostMapping
    public ResponseEntity<ReportResponse> create(@Valid @RequestBody CreateReportRequest request) {
        UUID reporterId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(reportService.create(reporterId, request));
    }

    @Operation(summary = "Мои жалобы")
    @GetMapping("/my")
    public ResponseEntity<Page<ReportResponse>> getMy(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID reporterId = securityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(reportService.getMyReports(reporterId, pageable));
    }

    @Operation(summary = "Все жалобы (для модератора)")
    @GetMapping
    public ResponseEntity<Page<ReportResponse>> getAll(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(reportService.getAll(status, pageable));
    }

    @Operation(summary = "Жалоба по ID")
    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(reportService.getById(id));
    }

    @Operation(summary = "Решить жалобу (RESOLVED / REJECTED) — для модератора")
    @PostMapping("/{id}/resolve")
    public ResponseEntity<ReportResponse> resolve(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveReportRequest request) {
        UUID moderatorId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(reportService.resolve(id, moderatorId, request));
    }
}

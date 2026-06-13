package backend.saferent.service;

import backend.saferent.dto.request.report.CreateReportRequest;
import backend.saferent.dto.request.report.ResolveReportRequest;
import backend.saferent.dto.response.report.ReportResponse;
import backend.saferent.entity.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReportService {

    ReportResponse create(UUID reporterId, CreateReportRequest request);

    ReportResponse getById(UUID id);

    Page<ReportResponse> getMyReports(UUID reporterId, Pageable pageable);

    Page<ReportResponse> getAll(ReportStatus status, Pageable pageable);

    ReportResponse resolve(UUID id, UUID moderatorId, ResolveReportRequest request);
}

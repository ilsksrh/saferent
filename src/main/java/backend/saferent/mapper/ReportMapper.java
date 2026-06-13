package backend.saferent.mapper;

import backend.saferent.dto.response.report.ReportResponse;
import backend.saferent.entity.Report;
import org.springframework.stereotype.Component;

@Component
public class ReportMapper {

    public ReportResponse toResponse(Report r) {
        return ReportResponse.builder()
                .id(r.getId())
                .reporterId(r.getReporter() != null ? r.getReporter().getId() : null)
                .reporterName(r.getReporter() != null ? r.getReporter().getName() : null)
                .targetUserId(r.getTargetUser() != null ? r.getTargetUser().getId() : null)
                .targetUserName(r.getTargetUser() != null ? r.getTargetUser().getName() : null)
                .apartmentId(r.getApartment() != null ? r.getApartment().getId() : null)
                .apartmentTitle(r.getApartment() != null ? r.getApartment().getTitle() : null)
                .messageId(r.getMessage() != null ? r.getMessage().getId() : null)
                .reason(r.getReason())
                .description(r.getDescription())
                .status(r.getStatus())
                .moderatorId(r.getModerator() != null ? r.getModerator().getId() : null)
                .moderatorName(r.getModerator() != null ? r.getModerator().getName() : null)
                .resolutionComment(r.getResolutionComment())
                .resolvedAt(r.getResolvedAt())
                .createdAt(r.getCreatedAt())
                .build();
    }
}

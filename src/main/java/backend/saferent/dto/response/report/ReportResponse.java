package backend.saferent.dto.response.report;

import backend.saferent.entity.enums.ReportReason;
import backend.saferent.entity.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {

    private UUID id;
    private UUID reporterId;
    private String reporterName;
    private UUID targetUserId;
    private String targetUserName;
    private UUID apartmentId;
    private String apartmentTitle;
    private UUID messageId;
    private ReportReason reason;
    private String description;
    private ReportStatus status;
    private UUID moderatorId;
    private String moderatorName;
    private String resolutionComment;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
}

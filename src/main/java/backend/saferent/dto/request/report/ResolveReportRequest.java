package backend.saferent.dto.request.report;

import backend.saferent.entity.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResolveReportRequest {

    @NotNull(message = "Статус обязателен (RESOLVED или REJECTED)")
    private ReportStatus status;

    private String comment;
}

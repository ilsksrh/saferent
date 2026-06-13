package backend.saferent.dto.request.report;

import backend.saferent.entity.enums.ReportReason;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateReportRequest {

    private UUID targetUserId;
    private UUID apartmentId;
    private UUID messageId;

    @NotNull(message = "Причина обязательна")
    private ReportReason reason;

    private String description;
}

package backend.saferent.dto.response.inspection;

import backend.saferent.entity.enums.InspectionType;
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
public class InspectionPhotoResponse {

    private UUID id;
    private UUID contractId;
    private InspectionType type;
    private String url;
    private String roomLabel;
    private Double ssimScore;
    private String damageDescription;
    private LocalDateTime createdAt;
}
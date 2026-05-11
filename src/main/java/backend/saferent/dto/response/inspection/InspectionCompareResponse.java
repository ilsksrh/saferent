package backend.saferent.dto.response.inspection;

import backend.saferent.entity.enums.InspectionResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionCompareResponse {

    private UUID contractId;
    private Double averageSsimScore;
    private Map<String, Double> roomScores;
    private InspectionResult result;
    private String depositDecision;
    private List<String> damagedRooms;
    private String summary;
}
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

    // Средний SSIM по всем комнатам
    private Double averageSsimScore;

    // Результат по каждой комнате
    private Map<String, Double> roomScores;

    // Итоговый вердикт
    private InspectionResult result;

    // Что делать с депозитом
    private String depositDecision;

    // Список комнат с повреждениями
    private List<String> damagedRooms;

    // Человекочитаемый вывод
    private String summary;
}
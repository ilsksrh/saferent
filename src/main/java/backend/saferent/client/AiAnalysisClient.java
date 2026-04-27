package backend.saferent.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiAnalysisClient {

    private final RestTemplate restTemplate;

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    public AiCompareResult comparePhotos(String beforeUrl, String afterUrl) {
        try {
            String url = aiServiceUrl + "/compare";

            CompareRequest request = new CompareRequest(beforeUrl, afterUrl);

            ResponseEntity<AiCompareResult> response = restTemplate.postForEntity(
                    url, request, AiCompareResult.class
            );

            if (response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("AI service unavailable: {}", e.getMessage());
            // Если AI недоступен — возвращаем mock результат для MVP
            return mockResult();
        }

        return mockResult();
    }

    private AiCompareResult mockResult() {
        AiCompareResult mock = new AiCompareResult();
        mock.setSsimScore(0.95);
        mock.setDamageRegionCount(0);
        mock.setDamageDescription("AI service unavailable — mock result: no damage detected");
        return mock;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompareRequest {
        private String beforeUrl;
        private String afterUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiCompareResult {
        private Double ssimScore;
        private Integer damageRegionCount;
        private String damageDescription;
    }
}
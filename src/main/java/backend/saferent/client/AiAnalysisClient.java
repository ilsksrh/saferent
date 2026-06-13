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

            if (response.getBody() == null) {
                throw new AiServiceUnavailableException("AI сервис вернул пустой ответ");
            }
            return response.getBody();
        } catch (AiServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI service unavailable: {}", e.getMessage());
            throw new AiServiceUnavailableException(
                    "AI сервис недоступен. Запустите ai-service и попробуйте снова."
            );
        }
    }

    public static class AiServiceUnavailableException extends RuntimeException {
        public AiServiceUnavailableException(String message) {
            super(message);
        }
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

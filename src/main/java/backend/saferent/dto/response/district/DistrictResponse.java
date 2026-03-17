package backend.saferent.dto.response.district;

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
public class DistrictResponse {
    private UUID id;
    private String name;
    private Integer safetyScore;
    private Integer comfortScore;
    private String colorCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
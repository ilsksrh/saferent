package backend.saferent.mapper;

import backend.saferent.dto.request.district.DistrictCreateRequest;
import backend.saferent.dto.response.district.DistrictResponse;
import backend.saferent.entity.District;
import org.springframework.stereotype.Component;

@Component
public class DistrictMapper {

    public District toEntity(DistrictCreateRequest request) {
        return District.builder()
                .name(request.getName())
                .safetyScore(request.getSafetyScore())
                .comfortScore(request.getComfortScore())
                .colorCode(request.getColorCode())
                .build();
    }

    public DistrictResponse toResponse(District district) {
        return DistrictResponse.builder()
                .id(district.getId())
                .name(district.getName())
                .safetyScore(district.getSafetyScore())
                .comfortScore(district.getComfortScore())
                .colorCode(district.getColorCode())
                .createdAt(district.getCreatedAt())
                .updatedAt(district.getUpdatedAt())
                .build();
    }
}
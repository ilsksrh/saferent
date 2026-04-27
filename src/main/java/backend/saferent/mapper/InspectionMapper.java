package backend.saferent.mapper;

import backend.saferent.dto.response.inspection.InspectionPhotoResponse;
import backend.saferent.entity.InspectionPhoto;
import org.springframework.stereotype.Component;

@Component
public class InspectionMapper {

    public InspectionPhotoResponse toResponse(InspectionPhoto p) {
        return InspectionPhotoResponse.builder()
                .id(p.getId())
                .contractId(p.getContract().getId())
                .type(p.getType())
                .url(p.getUrl())
                .roomLabel(p.getRoomLabel())
                .ssimScore(p.getSsimScore())
                .damageDescription(p.getDamageDescription())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
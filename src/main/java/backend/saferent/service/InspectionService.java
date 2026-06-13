package backend.saferent.service;

import backend.saferent.dto.response.inspection.InspectionCompareResponse;
import backend.saferent.dto.response.inspection.InspectionPhotoResponse;

import java.util.List;
import java.util.UUID;

public interface InspectionService {

    InspectionPhotoResponse uploadCheckinPhoto(
            UUID contractId, UUID uploadedBy,
            String photoUrl, String roomLabel
    );

    InspectionPhotoResponse uploadCheckoutPhoto(
            UUID contractId, UUID uploadedBy,
            String photoUrl, String roomLabel
    );

    InspectionCompareResponse compare(UUID contractId, UUID requestedBy);

    List<InspectionPhotoResponse> getAllByContract(UUID contractId);

    void deletePhoto(UUID contractId, UUID photoId, UUID currentUserId);

    void clearAll(UUID contractId, UUID currentUserId);
}
package backend.saferent.service;

import backend.saferent.dto.response.user.LandlordProfileResponse;
import backend.saferent.dto.response.user.LandlordApartmentSummary;
import java.util.List;
import java.util.UUID;

public interface LandlordService {

    LandlordProfileResponse getLandlordProfile(UUID userId);

    List<LandlordApartmentSummary> getLandlordApartments(UUID userId);

    void setUserAsLandlord(UUID userId);
}
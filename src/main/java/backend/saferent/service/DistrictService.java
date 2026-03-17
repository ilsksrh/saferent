package backend.saferent.service;

import backend.saferent.dto.request.district.DistrictCreateRequest;
import backend.saferent.dto.request.district.DistrictRatingRequest;
import backend.saferent.dto.response.district.DistrictResponse;
import java.util.List;
import java.util.UUID;

public interface DistrictService {
    DistrictResponse createDistrict(DistrictCreateRequest request);

    List<DistrictResponse> getAllDistricts();

    DistrictResponse getById(UUID id);

    void rateDistrict(DistrictRatingRequest request, UUID userId);
}
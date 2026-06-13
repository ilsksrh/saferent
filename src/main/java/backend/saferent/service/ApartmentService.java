package backend.saferent.service;

import backend.saferent.dto.request.apartment.CreateApartmentRequest;
import backend.saferent.dto.request.apartment.UpdateApartmentRequest;
import backend.saferent.dto.response.apartment.ApartmentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ApartmentService {

    ApartmentResponse createApartment(CreateApartmentRequest request);

    ApartmentResponse getById(UUID id);

    List<ApartmentResponse> getAllActive();

    List<ApartmentResponse> getByLandlord(UUID landlordId);

    ApartmentResponse updateApartment(UUID id, UpdateApartmentRequest request);

    void deleteApartment(UUID id); // soft delete

    ApartmentResponse verifyApartment(UUID id);

    ApartmentResponse rejectApartment(UUID id, String reason);

    List<ApartmentResponse> getPendingForModeration();

    ApartmentResponse addPhoto(UUID apartmentId, MultipartFile file, UUID currentUserId);

    void deletePhoto(UUID apartmentId, UUID photoId, UUID currentUserId);
}
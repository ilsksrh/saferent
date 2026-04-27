package backend.saferent.service;

import backend.saferent.dto.request.apartment.CreateApartmentRequest;
import backend.saferent.dto.request.apartment.UpdateApartmentRequest;
import backend.saferent.dto.response.apartment.ApartmentResponse;

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
}
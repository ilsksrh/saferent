package backend.saferent.service.impl;

import backend.saferent.dto.request.apartment.CreateApartmentRequest;
import backend.saferent.dto.request.apartment.UpdateApartmentRequest;
import backend.saferent.dto.response.ApartmentResponse;
import backend.saferent.entity.Apartment;
import backend.saferent.entity.District;
import backend.saferent.entity.User;
import backend.saferent.entity.enums.ApartmentStatus;
import backend.saferent.mapper.ApartmentMapper;
import backend.saferent.repository.ApartmentRepository;
import backend.saferent.repository.DistrictRepository;
import backend.saferent.repository.UserRepository;
import backend.saferent.service.ApartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApartmentServiceImpl implements ApartmentService {

    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;
    private final DistrictRepository districtRepository;
    private final ApartmentMapper apartmentMapper;

    @Override
    public ApartmentResponse createApartment(CreateApartmentRequest request) {
        User landlord = userRepository.findById(request.getLandlordId())
                .orElseThrow(() -> new RuntimeException("Арендодатель не найден"));

        District district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new RuntimeException("Район не найден"));

        Apartment apartment = apartmentMapper.toEntity(request, landlord, district);
        apartment = apartmentRepository.save(apartment);

        return apartmentMapper.toResponse(apartment);
    }

    @Override
    public ApartmentResponse getById(UUID id) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Квартира не найдена"));
        return apartmentMapper.toResponse(apartment);
    }

    @Override
    public List<ApartmentResponse> getAllActive() {
        return apartmentRepository.findAllByStatusAndDeletedAtIsNull(ApartmentStatus.ACTIVE)
                .stream()
                .map(apartmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApartmentResponse> getByLandlord(UUID landlordId) {
        return apartmentRepository.findAllByLandlordId(landlordId)
                .stream()
                .map(apartmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ApartmentResponse updateApartment(UUID id, UpdateApartmentRequest request) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Квартира не найдена"));

        apartmentMapper.updateEntity(apartment, request);
        apartment = apartmentRepository.save(apartment);

        return apartmentMapper.toResponse(apartment);
    }

    @Override
    public void deleteApartment(UUID id) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Квартира не найдена"));

        apartment.setDeletedAt(LocalDateTime.now());
        apartment.setStatus(ApartmentStatus.ARCHIVED);
        apartmentRepository.save(apartment);
    }
}
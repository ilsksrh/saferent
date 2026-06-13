package backend.saferent.mapper;

import backend.saferent.dto.request.apartment.CreateApartmentRequest;
import backend.saferent.dto.request.apartment.UpdateApartmentRequest;
import backend.saferent.dto.response.apartment.ApartmentPhotoDto;
import backend.saferent.dto.response.apartment.ApartmentResponse;
import backend.saferent.entity.Apartment;
import backend.saferent.entity.ApartmentPhoto;
import backend.saferent.entity.District;
import backend.saferent.entity.User;
import backend.saferent.entity.enums.ApartmentStatus;
import backend.saferent.repository.ApartmentPhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ApartmentMapper {

    private final ApartmentPhotoRepository apartmentPhotoRepository;

    public Apartment toEntity(CreateApartmentRequest request, User landlord, District district) {
        return Apartment.builder()
                .landlord(landlord)
                .district(district)
                .title(request.getTitle())
                .description(request.getDescription())
                .address(request.getAddress())
                .price(request.getPrice())
                .area(request.getArea())
                .rooms(request.getRooms())
                .availableFrom(request.getAvailableFrom())
                .verified(request.getVerified() != null ? request.getVerified() : false)
                .status(request.getStatus() != null ? request.getStatus() : ApartmentStatus.ACTIVE)
                .build();
    }

    public Apartment updateEntity(Apartment apartment, UpdateApartmentRequest request) {
        if (request.getTitle() != null) apartment.setTitle(request.getTitle());
        if (request.getDescription() != null) apartment.setDescription(request.getDescription());
        if (request.getAddress() != null) apartment.setAddress(request.getAddress());
        if (request.getPrice() != null) apartment.setPrice(request.getPrice());
        if (request.getArea() != null) apartment.setArea(request.getArea());
        if (request.getRooms() != null) apartment.setRooms(request.getRooms());
        if (request.getAvailableFrom() != null) apartment.setAvailableFrom(request.getAvailableFrom());
        if (request.getVerified() != null) apartment.setVerified(request.getVerified());
        if (request.getStatus() != null) apartment.setStatus(request.getStatus());
        return apartment;
    }

    public ApartmentResponse toResponse(Apartment apartment) {
        List<String> photoUrls = List.of();
        List<ApartmentPhotoDto> photos = List.of();

        if(apartmentPhotoRepository != null && apartment.getId() != null) {
            List<ApartmentPhoto> all = apartmentPhotoRepository.findByApartment(apartment);
            photoUrls = all.stream().map(ApartmentPhoto::getUrl).collect(Collectors.toList());
            photos = all.stream()
                    .map(p -> ApartmentPhotoDto.builder()
                            .id(p.getId())
                            .url(p.getUrl())
                            .position(p.getPosition())
                            .build())
                    .collect(Collectors.toList());
        }

        return ApartmentResponse.builder()
                .id(apartment.getId())
                .landlordId(apartment.getLandlord().getId())
                .districtId(apartment.getDistrict().getId())
                .title(apartment.getTitle())
                .description(apartment.getDescription())
                .address(apartment.getAddress())
                .price(apartment.getPrice())
                .photoUrls(photoUrls)
                .photos(photos)
                .area(apartment.getArea())
                .rooms(apartment.getRooms())
                .availableFrom(apartment.getAvailableFrom())
                .verified(apartment.isVerified())
                .rejectionReason(apartment.getRejectionReason())
                .rejectedAt(apartment.getRejectedAt())
                .status(apartment.getStatus())
                .createdAt(apartment.getCreatedAt())
                .updatedAt(apartment.getUpdatedAt())
                .deletedAt(apartment.getDeletedAt())
                .build();
    }
}
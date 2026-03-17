package backend.saferent.mapper;

import backend.saferent.dto.response.user.LandlordProfileResponse;
import backend.saferent.dto.response.user.LandlordApartmentSummary;
import backend.saferent.entity.Apartment;
import backend.saferent.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LandlordMapper {

    public LandlordProfileResponse toProfileResponse(User user) {
        return LandlordProfileResponse.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .name(user.getName())
                .preferredRole(user.getPreferredRole())
                .egovId(user.getEgovId())
                .verified(user.isVerified())
                .avatarUrl(user.getAvatarUrl())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .totalApartments(user.getApartments().size())
                .apartmentIds(user.getApartments().stream().map(Apartment::getId).collect(Collectors.toList()))
                .build();
    }

    public LandlordApartmentSummary toApartmentSummary(Apartment apartment) {
        return new LandlordApartmentSummary();
        // Заполни поля по необходимости:
        // .id(apartment.getId())
        // .title(apartment.getTitle())
        // .address(apartment.getAddress())
        // .price(apartment.getPrice())
        // .status(apartment.getStatus())
        // .createdAt(apartment.getCreatedAt())
    }
}
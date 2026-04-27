package backend.saferent.mapper;

import backend.saferent.dto.response.favorite.FavoriteResponse;
import backend.saferent.entity.Favorite;
import org.springframework.stereotype.Component;

@Component
public class FavoriteMapper {

    public FavoriteResponse toResponse(Favorite f) {
        return FavoriteResponse.builder()
                .id(f.getId())
                .userId(f.getUser().getId())
                .apartmentId(f.getApartment().getId())
                .apartmentTitle(f.getApartment().getTitle())
                .apartmentAddress(f.getApartment().getAddress())
                .apartmentPrice(f.getApartment().getPrice())
                .apartmentRooms(f.getApartment().getRooms())
                .apartmentVerified(f.getApartment().isVerified())
                .districtName(f.getApartment().getDistrict().getName())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
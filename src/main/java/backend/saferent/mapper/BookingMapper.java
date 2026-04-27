package backend.saferent.mapper;

import backend.saferent.dto.response.booking.BookingResponse;
import backend.saferent.entity.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .apartmentId(booking.getApartment().getId())
                .apartmentTitle(booking.getApartment().getTitle())
                .apartmentAddress(booking.getApartment().getAddress())
                .tenantId(booking.getTenant().getId())
                .tenantName(booking.getTenant().getName())
                .tenantPhone(booking.getTenant().getPhone())
                .landlordId(booking.getApartment().getLandlord().getId())
                .landlordName(booking.getApartment().getLandlord().getName())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .message(booking.getMessage())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
package backend.saferent.dto.response.user;

import backend.saferent.entity.enums.ApartmentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LandlordApartmentSummary {
    private UUID id;
    private String title;
    private String address;
    private BigDecimal price;
    private ApartmentStatus status;
    private LocalDateTime createdAt;
}
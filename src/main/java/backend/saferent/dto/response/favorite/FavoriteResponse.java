package backend.saferent.dto.response.favorite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponse {

    private UUID id;
    private UUID userId;

    // Информация о квартире
    private UUID apartmentId;
    private String apartmentTitle;
    private String apartmentAddress;
    private BigDecimal apartmentPrice;
    private Short apartmentRooms;
    private Boolean apartmentVerified;
    private String districtName;

    private LocalDateTime createdAt;
}
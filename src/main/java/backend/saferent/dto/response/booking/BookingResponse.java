package backend.saferent.dto.response.booking;

import backend.saferent.entity.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private UUID id;

    private UUID apartmentId;
    private String apartmentTitle;
    private String apartmentAddress;

    private UUID tenantId;
    private String tenantName;
    private String tenantPhone;

    private UUID landlordId;
    private String landlordName;

    private LocalDate startDate;
    private LocalDate endDate;

    private String message;
    private BookingStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
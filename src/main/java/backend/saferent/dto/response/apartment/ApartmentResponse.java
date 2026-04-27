package backend.saferent.dto.response.apartment;

import backend.saferent.entity.enums.ApartmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApartmentResponse {

    private UUID id;
    private UUID landlordId;
    private UUID districtId;
    private String title;
    private String description;
    private String address;
    private BigDecimal price;
    private BigDecimal area;
    private Short rooms;
    private LocalDate availableFrom;
    private Boolean verified;
    private ApartmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private List<String> photoUrls;
}
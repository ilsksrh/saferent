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
    private BigDecimal depositAmount;
    private BigDecimal area;
    private Short rooms;
    private LocalDate availableFrom;
    private Double latitude;
    private Double longitude;
    private String checkInTime;
    private String checkOutTime;
    private Integer maxGuests;
    private String cancellationPolicy;
    private String houseRules;
    private Boolean smokeAlarm;
    private Boolean securityCameras;
    private List<String> amenities;
    private List<String> panoramaUrls;
    private Boolean verified;
    private String rejectionReason;
    private LocalDateTime rejectedAt;
    private ApartmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private List<String> photoUrls;
    private List<ApartmentPhotoDto> photos;
}
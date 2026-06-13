package backend.saferent.dto.request.apartment;

import backend.saferent.entity.enums.ApartmentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateApartmentRequest {

    @Size(max = 150)
    private String title;

    private String description;

    @Size(max = 255)
    private String address;

    @DecimalMin("0.0")
    private BigDecimal price;

    @DecimalMin("0.0")
    private BigDecimal area;

    @Min(1)
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

    private Boolean verified;

    private ApartmentStatus status;
}
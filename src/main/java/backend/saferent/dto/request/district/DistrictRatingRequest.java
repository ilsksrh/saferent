package backend.saferent.dto.request.district;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class DistrictRatingRequest {

    @NotNull
    private UUID districtId;

    @Min(1) @Max(10)
    private Integer safetyRating;

    @Min(1) @Max(10)
    private Integer comfortRating;
}
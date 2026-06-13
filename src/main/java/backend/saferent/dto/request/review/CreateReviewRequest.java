package backend.saferent.dto.request.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateReviewRequest {

    @NotNull(message = "Contract ID is required")
    private UUID contractId;

    @NotNull(message = "Author ID is required")
    private UUID authorId;

    @NotNull(message = "Target user ID is required")
    private UUID targetUserId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating minimum is 1")
    @Max(value = 5, message = "Rating maximum is 5")
    private Short rating;

    @Size(max = 1000, message = "Comment too long")
    private String comment;

    @Min(1) @Max(5) private Short cleanliness;
    @Min(1) @Max(5) private Short accuracy;
    @Min(1) @Max(5) private Short checkin;
    @Min(1) @Max(5) private Short communication;
    @Min(1) @Max(5) private Short location;
    @Min(1) @Max(5) private Short value;
}
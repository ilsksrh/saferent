package backend.saferent.dto.response.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingBreakdownResponse {

    private UUID userId;
    private String userName;
    private double averageRating;
    private int totalReviews;
    private String ratingLabel;

    // distribution counts per star level
    private int star5;
    private int star4;
    private int star3;
    private int star2;
    private int star1;

    // per-category averages (null if no data)
    private Double cleanliness;
    private Double accuracy;
    private Double checkin;
    private Double communication;
    private Double location;
    private Double value;
}

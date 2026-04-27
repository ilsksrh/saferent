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
public class UserRatingResponse {

    private UUID userId;
    private String userName;
    private Double averageRating;     // средний рейтинг (1.0 - 5.0)
    private int totalReviews;         // количество отзывов
    private String ratingLabel;       // "Excellent" / "Good" / "Average" / "Poor"
}
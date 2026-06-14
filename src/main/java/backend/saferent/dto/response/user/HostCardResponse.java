package backend.saferent.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HostCardResponse {

    private UUID id;
    private String name;
    private String avatarUrl;
    private boolean verified;

    private String city;
    private String languages;
    private String bio;

    private LocalDateTime memberSince;
    private long monthsHosting;

    private double averageRating;
    private int totalReviews;
    private String ratingLabel;

    private int totalApartments;
    private boolean superhost;
}

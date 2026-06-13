package backend.saferent.dto.response.review;

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
public class ReviewResponse {

    private UUID id;

    private UUID contractId;
    private String apartmentTitle;

    private UUID authorId;
    private String authorName;

    private UUID targetUserId;
    private String targetUserName;

    private Short rating;
    private String comment;

    private Short cleanliness;
    private Short accuracy;
    private Short checkin;
    private Short communication;
    private Short location;
    private Short value;

    private LocalDateTime createdAt;
}
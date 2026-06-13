package backend.saferent.mapper;

import backend.saferent.dto.response.review.ReviewResponse;
import backend.saferent.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .contractId(r.getContract().getId())
                .apartmentTitle(r.getContract().getApartment().getTitle())
                .authorId(r.getAuthor().getId())
                .authorName(r.getAuthor().getName())
                .targetUserId(r.getTargetUser().getId())
                .targetUserName(r.getTargetUser().getName())
                .rating(r.getRating())
                .comment(r.getComment())
                .cleanliness(r.getCleanliness())
                .accuracy(r.getAccuracy())
                .checkin(r.getCheckin())
                .communication(r.getCommunication())
                .location(r.getLocation())
                .value(r.getValue())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
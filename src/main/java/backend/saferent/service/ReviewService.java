package backend.saferent.service;

import backend.saferent.dto.request.review.CreateReviewRequest;
import backend.saferent.dto.response.review.ReviewResponse;
import backend.saferent.dto.response.review.UserRatingResponse;

import java.util.List;
import java.util.UUID;

public interface ReviewService {

    ReviewResponse createReview(CreateReviewRequest request);

    List<ReviewResponse> getReviewsAboutUser(UUID userId);

    List<ReviewResponse> getReviewsByUser(UUID userId);

    UserRatingResponse getUserRating(UUID userId);
}
package backend.saferent.controller;

import backend.saferent.dto.request.review.CreateReviewRequest;
import backend.saferent.dto.response.review.RatingBreakdownResponse;
import backend.saferent.dto.response.review.ReviewResponse;
import backend.saferent.dto.response.review.UserRatingResponse;
import backend.saferent.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@Tag(name = "Reviews", description = "Отзывы после завершения аренды")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Оставить отзыв (только после COMPLETED договора)")
    @PostMapping
    public ResponseEntity<ReviewResponse> create(
            @Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.ok(reviewService.createReview(request));
    }

    @Operation(summary = "Отзывы О пользователе")
    @GetMapping("/about/{userId}")
    public ResponseEntity<List<ReviewResponse>> getAboutUser(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(reviewService.getReviewsAboutUser(userId));
    }

    @Operation(summary = "Отзывы написанные пользователем")
    @GetMapping("/by/{userId}")
    public ResponseEntity<List<ReviewResponse>> getByUser(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
    }

    @Operation(summary = "Рейтинг пользователя")
    @GetMapping("/rating/{userId}")
    public ResponseEntity<UserRatingResponse> getRating(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(reviewService.getUserRating(userId));
    }

    @Operation(summary = "Разбивка рейтинга (звёзды + категории)")
    @GetMapping("/breakdown/{userId}")
    public ResponseEntity<RatingBreakdownResponse> getBreakdown(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(reviewService.getRatingBreakdown(userId));
    }
}
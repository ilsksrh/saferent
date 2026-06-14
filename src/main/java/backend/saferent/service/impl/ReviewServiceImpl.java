package backend.saferent.service.impl;

import backend.saferent.dto.request.review.CreateReviewRequest;
import backend.saferent.dto.response.review.RatingBreakdownResponse;
import backend.saferent.dto.response.review.ReviewResponse;
import backend.saferent.dto.response.review.UserRatingResponse;
import backend.saferent.entity.Contract;
import backend.saferent.entity.Review;
import backend.saferent.entity.User;
import backend.saferent.entity.enums.ContractStatus;
import backend.saferent.entity.enums.NotificationType;
import backend.saferent.exception.BadRequestException;
import backend.saferent.exception.NotFoundException;
import backend.saferent.mapper.ReviewMapper;
import backend.saferent.repository.ContractRepository;
import backend.saferent.repository.ReviewRepository;
import backend.saferent.repository.UserRepository;
import backend.saferent.service.NotificationService;
import backend.saferent.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository   reviewRepository;
    private final ContractRepository contractRepository;
    private final UserRepository     userRepository;
    private final NotificationService notificationService;
    private final ReviewMapper       reviewMapper;


    @Override
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {

        Contract contract = contractRepository
                .findById(request.getContractId())
                .orElseThrow(() -> new NotFoundException("Contract not found"));

        if (contract.getStatus() != ContractStatus.ACTIVE
                && contract.getStatus() != ContractStatus.COMPLETED) {
            throw new BadRequestException(
                    "Отзыв можно оставить только если вы живёте или жили в этой квартире " +
                            "(договор ACTIVE или COMPLETED). Текущий статус: " + contract.getStatus()
            );
        }

        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new NotFoundException("Author not found"));

        User targetUser = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new NotFoundException("Target user not found"));

        boolean isTenant   = contract.getTenant().getId().equals(request.getAuthorId());
        boolean isLandlord = contract.getLandlord().getId().equals(request.getAuthorId());

        if (!isTenant && !isLandlord) {
            throw new BadRequestException(
                    "You are not a party to this contract"
            );
        }

        if (request.getAuthorId().equals(request.getTargetUserId())) {
            throw new BadRequestException("You cannot review yourself");
        }

        boolean targetIsTenant   = contract.getTenant().getId().equals(request.getTargetUserId());
        boolean targetIsLandlord = contract.getLandlord().getId().equals(request.getTargetUserId());

        if (!targetIsTenant && !targetIsLandlord) {
            throw new BadRequestException(
                    "Target user is not a party to this contract"
            );
        }

        if (reviewRepository.existsByContractAndAuthor(contract, author)) {
            throw new BadRequestException(
                    "You have already reviewed this contract"
            );
        }

        Review review = Review.builder()
                .contract(contract)
                .author(author)
                .targetUser(targetUser)
                .rating(request.getRating())
                .comment(request.getComment())
                .cleanliness(request.getCleanliness())
                .accuracy(request.getAccuracy())
                .checkin(request.getCheckin())
                .communication(request.getCommunication())
                .location(request.getLocation())
                .value(request.getValue())
                .build();

        Review saved = reviewRepository.save(review);

        notificationService.create(
                targetUser.getId(),
                "New Review from " + author.getName(),
                author.getName() + " gave you " + request.getRating() +
                        " stars for " + contract.getApartment().getTitle() +
                        (request.getComment() != null
                                ? ": \"" + request.getComment() + "\""
                                : ""),
                NotificationType.REVIEW,
                saved.getId(),
                "REVIEW"
        );

        return reviewMapper.toResponse(saved);
    }


    @Override
    public List<ReviewResponse> getReviewsAboutUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return reviewRepository.findByTargetUserOrderByCreatedAtDesc(user)
                .stream()
                .map(reviewMapper::toResponse)
                .collect(Collectors.toList());
    }


    @Override
    public List<ReviewResponse> getReviewsByUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return reviewRepository.findByAuthorOrderByCreatedAtDesc(user)
                .stream()
                .map(reviewMapper::toResponse)
                .collect(Collectors.toList());
    }


    @Override
    public UserRatingResponse getUserRating(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Double avg    = reviewRepository.getAverageRatingForUser(user);
        int    total  = reviewRepository.countByTargetUser(user);

        double rating = avg != null ? avg : 0.0;

        String label;
        if (rating >= 4.5)      label = "Excellent";
        else if (rating >= 3.5) label = "Good";
        else if (rating >= 2.5) label = "Average";
        else if (rating > 0)    label = "Poor";
        else                    label = "No reviews yet";

        return UserRatingResponse.builder()
                .userId(user.getId())
                .userName(user.getName())
                .averageRating(Math.round(rating * 10.0) / 10.0)
                .totalReviews(total)
                .ratingLabel(label)
                .build();
    }

    @Override
    public RatingBreakdownResponse getRatingBreakdown(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<Review> reviews = reviewRepository.findByTargetUserOrderByCreatedAtDesc(user);
        int total = reviews.size();

        int[] stars = new int[6]; // index 1..5
        double sum = 0;
        for (Review r : reviews) {
            if (r.getRating() != null) {
                stars[r.getRating()]++;
                sum += r.getRating();
            }
        }
        double avg = total > 0 ? sum / total : 0.0;

        String label;
        if (avg >= 4.5)      label = "Excellent";
        else if (avg >= 3.5) label = "Good";
        else if (avg >= 2.5) label = "Average";
        else if (avg > 0)    label = "Poor";
        else                 label = "No reviews yet";

        return RatingBreakdownResponse.builder()
                .userId(user.getId())
                .userName(user.getName())
                .averageRating(Math.round(avg * 10.0) / 10.0)
                .totalReviews(total)
                .ratingLabel(label)
                .star5(stars[5]).star4(stars[4]).star3(stars[3]).star2(stars[2]).star1(stars[1])
                .cleanliness(categoryAvg(reviews, Review::getCleanliness))
                .accuracy(categoryAvg(reviews, Review::getAccuracy))
                .checkin(categoryAvg(reviews, Review::getCheckin))
                .communication(categoryAvg(reviews, Review::getCommunication))
                .location(categoryAvg(reviews, Review::getLocation))
                .value(categoryAvg(reviews, Review::getValue))
                .build();
    }

    private Double categoryAvg(List<Review> reviews, Function<Review, Short> getter) {
        var stats = reviews.stream()
                .map(getter)
                .filter(Objects::nonNull)
                .mapToInt(Short::intValue)
                .average();
        return stats.isPresent() ? Math.round(stats.getAsDouble() * 10.0) / 10.0 : null;
    }
}
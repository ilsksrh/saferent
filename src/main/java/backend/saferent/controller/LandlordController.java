package backend.saferent.controller;

import backend.saferent.dto.response.analytics.LandlordAnalyticsResponse;
import backend.saferent.dto.response.review.UserRatingResponse;
import backend.saferent.dto.response.user.HostCardResponse;
import backend.saferent.service.LandlordAnalyticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import backend.saferent.dto.response.user.LandlordProfileResponse;
import backend.saferent.dto.response.user.LandlordApartmentSummary;
import backend.saferent.dto.response.user.LandlordSearchResponse;
import backend.saferent.entity.User;
import backend.saferent.entity.enums.PreferredRole;
import backend.saferent.exception.NotFoundException;
import backend.saferent.repository.UserRepository;
import backend.saferent.service.LandlordService;
import backend.saferent.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/landlords")
@Tag(name = "Landlords", description = "Функции для арендодателей")
@RequiredArgsConstructor
public class LandlordController {

    private final LandlordService landlordService;
    private final UserRepository userRepository;
    private final ReviewService reviewService;
    private final LandlordAnalyticsService landlordAnalyticsService;

    @Operation(summary = "Аналитика дохода лэндлорда (дашборд)")
    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping("/me/analytics")
    public ResponseEntity<LandlordAnalyticsResponse> myAnalytics() {
        return ResponseEntity.ok(landlordAnalyticsService.getMyAnalytics());
    }

    @Operation(summary = "Карточка хозяина (Meet your host)")
    @GetMapping("/{userId}/host")
    public ResponseEntity<HostCardResponse> hostCard(@PathVariable UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        UserRatingResponse rating = reviewService.getUserRating(userId);

        long months = user.getCreatedAt() != null
                ? ChronoUnit.MONTHS.between(user.getCreatedAt().toLocalDate(), LocalDate.now())
                : 0;
        boolean superhost = rating.getAverageRating() >= 4.8 && rating.getTotalReviews() >= 5;
        int totalApartments = user.getApartments() != null ? user.getApartments().size() : 0;

        return ResponseEntity.ok(HostCardResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .avatarUrl(user.getAvatarUrl())
                .verified(user.isVerified())
                .city(user.getCity())
                .languages(user.getLanguages())
                .bio(user.getBio())
                .memberSince(user.getCreatedAt())
                .monthsHosting(Math.max(0, months))
                .averageRating(rating.getAverageRating())
                .totalReviews(rating.getTotalReviews())
                .ratingLabel(rating.getRatingLabel())
                .totalApartments(totalApartments)
                .superhost(superhost)
                .build());
    }

    @Operation(summary = "Поиск арендодателей по ФИО")
    @GetMapping("/search")
    public ResponseEntity<List<LandlordSearchResponse>> search(@RequestParam String name) {
        if (name == null || name.isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        List<LandlordSearchResponse> result = userRepository
                .findByPreferredRoleAndNameContainingIgnoreCase(PreferredRole.LANDLORD, name.trim())
                .stream()
                .map(u -> LandlordSearchResponse.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .phone(u.getPhone())
                        .avatarUrl(u.getAvatarUrl())
                        .verified(u.isVerified())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Получить профиль арендодателя")
    @GetMapping("/{userId}")
    public ResponseEntity<LandlordProfileResponse> getProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(landlordService.getLandlordProfile(userId));
    }

    @Operation(summary = "Получить все объявления арендодателя")
    @GetMapping("/{userId}/apartments")
    public ResponseEntity<List<LandlordApartmentSummary>> getApartments(@PathVariable UUID userId) {
        return ResponseEntity.ok(landlordService.getLandlordApartments(userId));
    }

    @Operation(summary = "Сделать пользователя арендодателем")
    @PostMapping("/{userId}/promote")
    public ResponseEntity<String> promoteToLandlord(@PathVariable UUID userId) {
        landlordService.setUserAsLandlord(userId);
        return ResponseEntity.ok("Пользователь теперь арендодатель");
    }
}
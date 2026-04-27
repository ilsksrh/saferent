package backend.saferent.controller;

import backend.saferent.dto.response.favorite.FavoriteResponse;
import backend.saferent.service.FavoriteService;
import backend.saferent.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/favorites")
@Tag(name = "Favorites", description = "Избранные квартиры")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final SecurityUtils   securityUtils;

    @Operation(summary = "Добавить / убрать из избранного")
    @PostMapping("/toggle")
    public ResponseEntity<String> toggle(
            @RequestParam UUID apartmentId) {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(
                favoriteService.toggleFavorite(userId, apartmentId)
        );
    }

    @Operation(summary = "Мои избранные квартиры")
    @GetMapping("/my")
    public ResponseEntity<List<FavoriteResponse>> getMy() {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(favoriteService.getMyFavorites(userId));
    }

    @Operation(summary = "Проверить — в избранном ли квартира")
    @GetMapping("/check")
    public ResponseEntity<Boolean> check(
            @RequestParam UUID apartmentId) {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(
                favoriteService.isFavorite(userId, apartmentId)
        );
    }
}
package backend.saferent.controller;

import backend.saferent.dto.response.user.LandlordProfileResponse;
import backend.saferent.dto.response.user.LandlordApartmentSummary;
import backend.saferent.service.LandlordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/landlords")
@Tag(name = "Landlords", description = "Функции для арендодателей")
@RequiredArgsConstructor
public class LandlordController {

    private final LandlordService landlordService;

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
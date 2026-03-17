package backend.saferent.controller;

import backend.saferent.dto.request.district.DistrictCreateRequest;
import backend.saferent.dto.request.district.DistrictRatingRequest;
import backend.saferent.dto.response.district.DistrictResponse;
import backend.saferent.service.DistrictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/districts")
@Tag(name = "Districts", description = "Районы и их рейтинг (карта, оценки)")
@RequiredArgsConstructor
public class DistrictController {

    private final DistrictService districtService;

    @Operation(summary = "Создать новый район (для админа)")
    @PostMapping
    public ResponseEntity<DistrictResponse> create(@Valid @RequestBody DistrictCreateRequest request) {
        DistrictResponse response = districtService.createDistrict(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получить все районы (для карты)")
    @GetMapping
    public ResponseEntity<List<DistrictResponse>> getAll() {
        return ResponseEntity.ok(districtService.getAllDistricts());
    }

    @Operation(summary = "Получить район по ID")
    @GetMapping("/{id}")
    public ResponseEntity<DistrictResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(districtService.getById(id));
    }

    @Operation(summary = "Оставить оценку району")
    @PostMapping("/rate")
    public ResponseEntity<String> rate(
            @RequestBody DistrictRatingRequest request,
            @RequestParam UUID userId) {  // в будущем — из JWT

        districtService.rateDistrict(request, userId);
        return ResponseEntity.ok("Оценка сохранена");
    }
}
package backend.saferent.controller;

import backend.saferent.dto.request.apartment.CreateApartmentRequest;
import backend.saferent.dto.request.apartment.UpdateApartmentRequest;
import backend.saferent.service.ApartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import backend.saferent.dto.response.apartment.ApartmentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/apartments")
@Tag(name = "Apartments", description = "Управление объявлениями о квартирах")
@RequiredArgsConstructor
public class ApartmentController {

    private final ApartmentService apartmentService;

    @Operation(summary = "Создать новое объявление")
    @PostMapping
    public ResponseEntity<ApartmentResponse> create(@RequestBody CreateApartmentRequest request) {
        return ResponseEntity.ok(apartmentService.createApartment(request));
    }

    @Operation(summary = "Получить объявление по ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApartmentResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(apartmentService.getById(id));
    }

    @Operation(summary = "Получить все активные объявления (лента главной страницы)")
    @GetMapping("/active")
    public ResponseEntity<List<ApartmentResponse>> getAllActive() {
        return ResponseEntity.ok(apartmentService.getAllActive());
    }

    @Operation(summary = "Получить все объявления арендодателя")
    @GetMapping("/landlord/{landlordId}")
    public ResponseEntity<List<ApartmentResponse>> getByLandlord(@PathVariable UUID landlordId) {
        return ResponseEntity.ok(apartmentService.getByLandlord(landlordId));
    }

    @Operation(summary = "Обновить объявление")
    @PutMapping("/{id}")
    public ResponseEntity<ApartmentResponse> update(@PathVariable UUID id,
                                                    @RequestBody UpdateApartmentRequest request) {
        return ResponseEntity.ok(apartmentService.updateApartment(id, request));
    }

    @Operation(summary = "Удалить объявление (soft delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        apartmentService.deleteApartment(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Верифицировать квартиру (admin/landlord)")
    @PostMapping("/{id}/verify")
    public ResponseEntity<ApartmentResponse> verify(@PathVariable UUID id) {
        return ResponseEntity.ok(apartmentService.verifyApartment(id));
    }
}
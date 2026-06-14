package backend.saferent.controller;

import backend.saferent.dto.request.apartment.CreateApartmentRequest;
import backend.saferent.dto.request.apartment.RejectApartmentRequest;
import backend.saferent.dto.request.apartment.UpdateApartmentRequest;
import backend.saferent.entity.User;
import jakarta.validation.Valid;
import backend.saferent.search.ApartmentDocument;
import backend.saferent.search.ApartmentSearchService;
import backend.saferent.service.ApartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import backend.saferent.dto.response.apartment.ApartmentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/apartments")
@Tag(name = "Apartments", description = "Управление объявлениями о квартирах")
@RequiredArgsConstructor
public class ApartmentController {

    private final ApartmentService apartmentService;
    private final ApartmentSearchService apartmentSearchService;

    @Operation(summary = "Полнотекстовый поиск квартир (Elasticsearch)")
    @GetMapping("/search")
    public ResponseEntity<List<ApartmentDocument>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) UUID districtId,
            @RequestParam(required = false) Short rooms) {
        return ResponseEntity.ok(apartmentSearchService.search(q, minPrice, maxPrice, districtId, rooms));
    }

    @Operation(summary = "Создать новое объявление (landlord = текущий пользователь)")
    @PostMapping
    public ResponseEntity<ApartmentResponse> create(@RequestBody CreateApartmentRequest request,
                                                    @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        request.setLandlordId(currentUser.getId());
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

    @Operation(summary = "Верифицировать квартиру (только админ)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/verify")
    public ResponseEntity<ApartmentResponse> verify(@PathVariable UUID id) {
        return ResponseEntity.ok(apartmentService.verifyApartment(id));
    }

    @Operation(summary = "Отклонить квартиру с причиной (только админ)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApartmentResponse> reject(
            @PathVariable UUID id,
            @Valid @RequestBody RejectApartmentRequest request) {
        return ResponseEntity.ok(apartmentService.rejectApartment(id, request.getReason()));
    }

    @Operation(summary = "Список квартир на модерации (только админ)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<List<ApartmentResponse>> getPending() {
        return ResponseEntity.ok(apartmentService.getPendingForModeration());
    }

    @Operation(summary = "Загрузить фото квартиры (multipart → MinIO)")
    @PostMapping(value = "/{id}/photos", consumes = "multipart/form-data")
    public ResponseEntity<ApartmentResponse> uploadPhoto(@PathVariable UUID id,
                                                         @RequestParam("file") MultipartFile file,
                                                         @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(apartmentService.addPhoto(id, file, currentUser.getId()));
    }

    @Operation(summary = "Удалить фото квартиры")
    @DeleteMapping("/{id}/photos/{photoId}")
    public ResponseEntity<Void> deletePhoto(@PathVariable UUID id,
                                            @PathVariable UUID photoId,
                                            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        apartmentService.deletePhoto(id, photoId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Загрузить 360° панораму (3D-тур) → MinIO")
    @PostMapping(value = "/{id}/panoramas", consumes = "multipart/form-data")
    public ResponseEntity<ApartmentResponse> uploadPanorama(@PathVariable UUID id,
                                                            @RequestParam("file") MultipartFile file,
                                                            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(apartmentService.addPanorama(id, file, currentUser.getId()));
    }

    @Operation(summary = "Удалить 360° панораму")
    @DeleteMapping("/{id}/panoramas")
    public ResponseEntity<ApartmentResponse> deletePanorama(@PathVariable UUID id,
                                                            @RequestParam String url,
                                                            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(apartmentService.removePanorama(id, url, currentUser.getId()));
    }
}
package backend.saferent.controller;

import backend.saferent.dto.response.inspection.InspectionCompareResponse;
import backend.saferent.dto.response.inspection.InspectionPhotoResponse;
import backend.saferent.exception.BadRequestException;
import backend.saferent.service.FileStorageService;
import backend.saferent.service.InspectionService;
import backend.saferent.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inspections")
@Tag(name = "Inspections", description = "Фото при заезде/выезде и AI анализ")
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService   inspectionService;
    private final FileStorageService  fileStorageService;
    private final SecurityUtils       securityUtils;

    @Operation(summary = "Загрузить фото при заезде (multipart-файл или photoUrl)")
    @PostMapping("/{contractId}/checkin")
    public ResponseEntity<InspectionPhotoResponse> uploadCheckin(
            @PathVariable UUID contractId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(required = false) String photoUrl,
            @RequestParam(required = false) String roomLabel) {

        String url = resolvePhotoUrl(file, photoUrl, contractId, "checkin");
        UUID uploadedBy = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(inspectionService.uploadCheckinPhoto(
                contractId, uploadedBy, url, roomLabel
        ));
    }

    @Operation(summary = "Тенант подтверждает фото при заезде")
    @PostMapping("/{contractId}/confirm-checkin")
    public ResponseEntity<Void> confirmCheckin(@PathVariable UUID contractId) {
        inspectionService.confirmCheckin(contractId, securityUtils.getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Загрузить фото при выезде (multipart-файл или photoUrl)")
    @PostMapping("/{contractId}/checkout")
    public ResponseEntity<InspectionPhotoResponse> uploadCheckout(
            @PathVariable UUID contractId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(required = false) String photoUrl,
            @RequestParam(required = false) String roomLabel) {

        String url = resolvePhotoUrl(file, photoUrl, contractId, "checkout");
        UUID uploadedBy = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(inspectionService.uploadCheckoutPhoto(
                contractId, uploadedBy, url, roomLabel
        ));
    }

    @Operation(summary = "Запустить AI сравнение фото")
    @PostMapping("/{contractId}/compare")
    public ResponseEntity<InspectionCompareResponse> compare(
            @PathVariable UUID contractId) {
        UUID requestedBy = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(
                inspectionService.compare(contractId, requestedBy)
        );
    }

    @Operation(summary = "Все фото по договору")
    @GetMapping("/{contractId}")
    public ResponseEntity<List<InspectionPhotoResponse>> getAll(
            @PathVariable UUID contractId) {
        return ResponseEntity.ok(
                inspectionService.getAllByContract(contractId)
        );
    }

    @Operation(summary = "Удалить одно фото инспекции")
    @DeleteMapping("/{contractId}/photos/{photoId}")
    public ResponseEntity<Void> deletePhoto(
            @PathVariable UUID contractId,
            @PathVariable UUID photoId) {
        inspectionService.deletePhoto(contractId, photoId, securityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Очистить все фото инспекции (для повторной загрузки)")
    @DeleteMapping("/{contractId}/photos")
    public ResponseEntity<Void> clearAll(@PathVariable UUID contractId) {
        inspectionService.clearAll(contractId, securityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    private String resolvePhotoUrl(MultipartFile file, String photoUrl, UUID contractId, String stage) {
        if (file != null && !file.isEmpty()) {
            return fileStorageService.upload(file, "inspections/" + contractId + "/" + stage);
        }
        if (photoUrl != null && !photoUrl.isBlank()) {
            return photoUrl;
        }
        throw new BadRequestException("Нужно передать файл (file) или photoUrl");
    }
}

package backend.saferent.controller;

import backend.saferent.dto.response.inspection.InspectionCompareResponse;
import backend.saferent.dto.response.inspection.InspectionPhotoResponse;
import backend.saferent.service.InspectionService;
import backend.saferent.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inspections")
@Tag(name = "Inspections", description = "Фото при заезде/выезде и AI анализ")
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;
    private final SecurityUtils     securityUtils;

    @Operation(summary = "Загрузить фото при заезде")
    @PostMapping("/{contractId}/checkin")
    public ResponseEntity<InspectionPhotoResponse> uploadCheckin(
            @PathVariable UUID contractId,
            @RequestParam String photoUrl,
            @RequestParam(required = false) String roomLabel) {
        UUID uploadedBy = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(inspectionService.uploadCheckinPhoto(
                contractId, uploadedBy, photoUrl, roomLabel
        ));
    }

    @Operation(summary = "Загрузить фото при выезде")
    @PostMapping("/{contractId}/checkout")
    public ResponseEntity<InspectionPhotoResponse> uploadCheckout(
            @PathVariable UUID contractId,
            @RequestParam String photoUrl,
            @RequestParam(required = false) String roomLabel) {
        UUID uploadedBy = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(inspectionService.uploadCheckoutPhoto(
                contractId, uploadedBy, photoUrl, roomLabel
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
}
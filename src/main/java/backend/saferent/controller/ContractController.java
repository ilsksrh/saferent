package backend.saferent.controller;

import backend.saferent.dto.request.contract.CreateContractRequest;
import backend.saferent.dto.response.contract.ContractResponse;
import backend.saferent.service.ContractService;
import backend.saferent.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contracts")
@Tag(name = "Contracts", description = "Договоры аренды")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;
    private final SecurityUtils   securityUtils;

    @Operation(summary = "Создать договор вручную")
    @PostMapping
    public ResponseEntity<ContractResponse> create(
            @Valid @RequestBody CreateContractRequest request) {
        return ResponseEntity.ok(contractService.createContract(request));
    }

    @Operation(summary = "Получить договор по ID")
    @GetMapping("/{id}")
    public ResponseEntity<ContractResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(contractService.getById(id));
    }

    @Operation(summary = "Мои договоры")
    @GetMapping("/my")
    public ResponseEntity<List<ContractResponse>> getMy() {
        return ResponseEntity.ok(contractService.getMyContracts());
    }

    @Operation(summary = "Подписать договор")
    @PostMapping("/{id}/sign")
    public ResponseEntity<ContractResponse> sign(@PathVariable UUID id) {
        return ResponseEntity.ok(contractService.sign(id));
    }

    @Operation(summary = "Отменить договор")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ContractResponse> cancel(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(contractService.cancel(id, reason));
    }
}
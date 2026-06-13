package backend.saferent.controller;

import backend.saferent.dto.request.payment.CreatePaymentRequest;
import backend.saferent.dto.response.payment.EscrowStatusResponse;
import backend.saferent.dto.response.payment.PaymentResponse;
import backend.saferent.dto.response.payment.RentPeriodResponse;
import backend.saferent.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments & Escrow", description = "Платежи и эскроу депозит")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Оплатить депозит")
    @PostMapping("/deposit")
    public ResponseEntity<PaymentResponse> payDeposit(
            @Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.ok(paymentService.payDeposit(request));
    }

    @Operation(summary = "Оплатить аренду за месяц")
    @PostMapping("/rent")
    public ResponseEntity<PaymentResponse> payRent(
            @Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.ok(paymentService.payRent(request));
    }

    @Operation(summary = "История платежей по договору")
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<List<PaymentResponse>> getByContract(
            @PathVariable UUID contractId) {
        return ResponseEntity.ok(paymentService.getByContract(contractId));
    }

    @Operation(summary = "График платежей аренды (календарь)")
    @GetMapping("/schedule/{contractId}")
    public ResponseEntity<List<RentPeriodResponse>> getRentSchedule(
            @PathVariable UUID contractId) {
        return ResponseEntity.ok(paymentService.getRentSchedule(contractId));
    }

    @Operation(summary = "Статус эскроу по договору")
    @GetMapping("/escrow/{contractId}")
    public ResponseEntity<EscrowStatusResponse> getEscrowStatus(
            @PathVariable UUID contractId) {
        return ResponseEntity.ok(paymentService.getEscrowStatus(contractId));
    }

    @Operation(summary = "Вернуть депозит арендатору (SSIM >= 0.92)")
    @PostMapping("/escrow/{contractId}/release/tenant")
    public ResponseEntity<PaymentResponse> releaseToTenant(
            @PathVariable UUID contractId) {
        return ResponseEntity.ok(
                paymentService.releaseDepositToTenant(contractId)
        );
    }

    @Operation(summary = "Передать депозит арендодателю (SSIM < 0.70)")
    @PostMapping("/escrow/{contractId}/release/landlord")
    public ResponseEntity<PaymentResponse> releaseToLandlord(
            @PathVariable UUID contractId) {
        return ResponseEntity.ok(
                paymentService.releaseDepositToLandlord(contractId)
        );
    }
}
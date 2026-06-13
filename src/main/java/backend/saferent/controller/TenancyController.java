package backend.saferent.controller;

import backend.saferent.dto.response.contract.TenantOverviewResponse;
import backend.saferent.service.TenantOverviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tenancy")
@Tag(name = "Tenancy", description = "Текущая аренда и баланс арендатора")
@RequiredArgsConstructor
public class TenancyController {

    private final TenantOverviewService tenantOverviewService;

    @Operation(summary = "Моя текущая аренда + баланс")
    @GetMapping("/me")
    public ResponseEntity<TenantOverviewResponse> myTenancy() {
        return ResponseEntity.ok(tenantOverviewService.getMyTenancy());
    }
}

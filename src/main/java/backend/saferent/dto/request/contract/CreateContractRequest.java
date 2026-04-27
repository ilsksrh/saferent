package backend.saferent.dto.request.contract;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateContractRequest {

    @NotNull
    private UUID tenantId;

    @NotNull
    private UUID landlordId;

    @NotNull
    private UUID apartmentId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal rentAmount;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal depositAmount;

    private String terms;
}
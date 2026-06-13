package backend.saferent.dto.response.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantOverviewResponse {

    private boolean active;
    private UUID contractId;
    private UUID apartmentId;
    private String apartmentTitle;
    private String apartmentAddress;
    private BigDecimal rentAmount;
    private BigDecimal depositInEscrow;
    private BigDecimal outstandingRent;
    private LocalDate nextDueDate;
    private String contractStatus;
}

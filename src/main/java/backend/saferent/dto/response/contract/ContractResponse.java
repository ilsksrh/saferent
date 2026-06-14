package backend.saferent.dto.response.contract;

import backend.saferent.entity.enums.ContractStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractResponse {

    private UUID id;

    private UUID tenantId;
    private String tenantName;
    private String tenantPhone;

    private UUID landlordId;
    private String landlordName;
    private String landlordPhone;

    private UUID apartmentId;
    private String apartmentTitle;
    private String apartmentAddress;

    private LocalDate startDate;
    private LocalDate endDate;

    private BigDecimal rentAmount;
    private BigDecimal depositAmount;

    private ContractStatus status;
    private String statusReason;
    private String terms;

    private boolean tenantSigned;
    private boolean landlordSigned;
    private LocalDateTime signedAt;

    private LocalDate checkinDeadline;
    private LocalDateTime checkinConfirmedAt;
    private LocalDate checkoutDeadline;
    private backend.saferent.entity.enums.InspectionResult inspectionResult;
    private Double inspectionAvgSsim;
    private LocalDateTime inspectionDecidedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
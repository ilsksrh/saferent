package backend.saferent.dto.response.payment;

import backend.saferent.entity.enums.InspectionResult;
import backend.saferent.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscrowReviewResponse {

    private UUID contractId;
    private String apartmentTitle;
    private String tenantName;
    private String landlordName;
    private BigDecimal depositAmount;
    private InspectionResult inspectionResult;
    private Double inspectionAvgSsim;
    private LocalDateTime inspectionDecidedAt;
    private PaymentStatus depositStatus;
    private String recommendation;
}

package backend.saferent.dto.response.payment;

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
public class EscrowStatusResponse {

    private UUID contractId;
    private String apartmentTitle;

    private String tenantName;
    private String landlordName;

    private BigDecimal depositAmount;
    private PaymentStatus depositStatus;

    private boolean depositPaid;
    private LocalDateTime depositPaidAt;

    private String depositReturnDestination;

    private String escrowNote;
}
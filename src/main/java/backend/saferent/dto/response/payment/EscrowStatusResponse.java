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
    private PaymentStatus depositStatus;    // PENDING / PAID / REFUNDED

    private boolean depositPaid;
    private LocalDateTime depositPaidAt;

    // Куда вернётся депозит (определяется после AI анализа)
    private String depositReturnDestination; // "TENANT" / "LANDLORD" / "PENDING_REVIEW"

    private String escrowNote;               // объяснение статуса
}
package backend.saferent.dto.response.payment;

import backend.saferent.entity.enums.RentPeriodStatus;
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
public class RentPeriodResponse {

    private UUID id;
    private int periodIndex;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate dueDate;
    private BigDecimal amount;
    private RentPeriodStatus status;
    private LocalDateTime paidAt;
}

package backend.saferent.dto.response.wallet;

import backend.saferent.entity.enums.WalletTxnType;
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
public class WalletTransactionResponse {

    private UUID id;
    private WalletTxnType type;
    private BigDecimal amount;        // signed
    private BigDecimal balanceAfter;
    private String description;
    private String apartmentTitle;
    private LocalDateTime createdAt;
}

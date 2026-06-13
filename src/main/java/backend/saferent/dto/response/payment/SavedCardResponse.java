package backend.saferent.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedCardResponse {

    private UUID id;
    private String brand;
    private String last4;
    private int expMonth;
    private int expYear;
    private boolean defaultCard;
}

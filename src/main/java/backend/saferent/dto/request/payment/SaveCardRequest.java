package backend.saferent.dto.request.payment;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SaveCardRequest {

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "[0-9 ]{13,23}", message = "Invalid card number")
    private String cardNumber;

    @Min(1) @Max(12)
    private int expMonth;

    @Min(2024) @Max(2099)
    private int expYear;

    /** Accepted from the form but never stored. */
    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "[0-9]{3,4}", message = "Invalid CVV")
    private String cvv;

    private boolean makeDefault = true;
}

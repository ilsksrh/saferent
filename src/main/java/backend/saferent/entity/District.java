package backend.saferent.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "district")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class District extends AbstractEntity {

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, unique = true)  // добавлено unique — районы не должны повторяться
    private String name;

    @Min(0)
    @Max(10)
    private Integer safetyScore;   // кэш, обновляется из DistrictRating

    @Min(0)
    @Max(10)
    private Integer comfortScore;  // кэш

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Неверный формат HEX цвета")
    @Column(nullable = false)
    private String colorCode;      // например #FF5733
}
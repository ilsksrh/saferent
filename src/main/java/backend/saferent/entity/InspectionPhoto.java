package backend.saferent.entity;

import backend.saferent.entity.enums.InspectionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionPhoto extends AbstractEntity {

    @ManyToOne
    @JoinColumn(nullable = false)
    private Contract contract;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionType type;       // CHECKIN / CHECKOUT

    @NotBlank
    @Size(max = 1024)
    private String url;                // URL фото в хранилище

    private String roomLabel;          // "kitchen", "bedroom", "bathroom", "living_room"

    private Double ssimScore;          // заполняется после AI анализа (0.0 - 1.0)

    private String damageDescription;  // описание повреждений от AI
}
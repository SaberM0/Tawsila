package com.covoiturage.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "points_arret")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointArret {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trajet_id", nullable = false)
    private Trajet trajet;

    @NotBlank(message = "La ville est obligatoire")
    @Column(name = "ville", nullable = false)
    private String ville;

    @NotNull(message = "L'ordre est obligatoire")
    @Min(value = 0, message = "L'ordre doit être supérieur ou égal à 0")
    @Column(name = "ordre", nullable = false)
    private Integer ordre;

    @NotNull(message = "L'heure de passage est obligatoire")
    @Column(name = "heure_passage", nullable = false)
    private LocalDateTime heurePassage;

    @NotNull(message = "Le prix depuis le départ est obligatoire")
    @DecimalMin(value = "0.00", message = "Le prix depuis le départ doit être positif ou nul")
    @Column(name = "prix_depuis_depart", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixDepuisDepart;
}

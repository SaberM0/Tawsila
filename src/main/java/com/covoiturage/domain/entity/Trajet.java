package com.covoiturage.domain.entity;

import com.covoiturage.domain.enums.StatutTrajet;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trajets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trajet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conducteur_id", nullable = false)
    private Conducteur conducteur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;

    @NotBlank(message = "La ville de départ est obligatoire")
    @Column(name = "ville_depart", nullable = false)
    private String villeDepart;

    @NotBlank(message = "La ville d'arrivée est obligatoire")
    @Column(name = "ville_arrivee", nullable = false)
    private String villeArrivee;

    @NotNull(message = "La date de départ est obligatoire")
    @Column(name = "date_depart", nullable = false)
    private LocalDateTime dateDepart;

    @NotNull(message = "Les places proposées sont obligatoires")
    @Min(value = 1, message = "Le nombre de places proposées doit être au moins 1")
    @Column(name = "places_proposees", nullable = false)
    private Integer placesProposees;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    @Builder.Default
    private StatutTrajet statut = StatutTrajet.PUBLIE;

    @OneToMany(mappedBy = "trajet", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordre ASC")
    @Builder.Default
    private List<PointArret> pointsArret = new ArrayList<>();

    @OneToMany(mappedBy = "trajet", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Reservation> reservations = new ArrayList<>();

    public void addPointArret(PointArret pointArret) {
        pointsArret.add(pointArret);
        pointArret.setTrajet(this);
    }
}

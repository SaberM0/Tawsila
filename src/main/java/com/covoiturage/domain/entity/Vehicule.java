package com.covoiturage.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehicules", uniqueConstraints = {
        @UniqueConstraint(name = "uk_vehicule_immatriculation", columnNames = "immatriculation")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La marque est obligatoire")
    @Column(name = "marque", nullable = false)
    private String marque;

    @NotBlank(message = "Le modèle est obligatoire")
    @Column(name = "modele", nullable = false)
    private String modele;

    @NotBlank(message = "L'immatriculation est obligatoire")
    @Column(name = "immatriculation", nullable = false, unique = true)
    private String immatriculation;

    @NotBlank(message = "La couleur est obligatoire")
    @Column(name = "couleur", nullable = false)
    private String couleur;

    @NotNull(message = "La capacité est obligatoire")
    @Min(value = 1, message = "La capacité minimale est de 1 place")
    @Max(value = 8, message = "La capacité maximale est de 8 places")
    @Column(name = "capacite", nullable = false)
    private Integer capacite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conducteur_id", nullable = false)
    private Conducteur conducteur;

    @OneToMany(mappedBy = "vehicule")
    @Builder.Default
    private List<Trajet> trajets = new ArrayList<>();
}

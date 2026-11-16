package com.covoiturage.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conducteurs", uniqueConstraints = {
        @UniqueConstraint(name = "uk_conducteur_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_conducteur_permis", columnNames = "numero_permis")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conducteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(name = "nom", nullable = false)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Column(name = "prenom", nullable = false)
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Column(name = "telephone", nullable = false)
    private String telephone;

    @NotBlank(message = "Le numéro de permis est obligatoire")
    @Column(name = "numero_permis", nullable = false, unique = true)
    private String numeroPermis;

    @OneToMany(mappedBy = "conducteur", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Vehicule> vehicules = new ArrayList<>();

    @OneToMany(mappedBy = "conducteur", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Trajet> trajets = new ArrayList<>();
}

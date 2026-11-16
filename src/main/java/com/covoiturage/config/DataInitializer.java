package com.covoiturage.config;

import com.covoiturage.domain.entity.*;
import com.covoiturage.domain.enums.StatutReservation;
import com.covoiturage.domain.enums.StatutTrajet;
import com.covoiturage.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ConducteurRepository conducteurRepository;
    private final PassagerRepository passagerRepository;
    private final VehiculeRepository vehiculeRepository;
    private final TrajetRepository trajetRepository;
    private final PointArretRepository pointArretRepository;
    private final ReservationRepository reservationRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (conducteurRepository.count() > 0) {
            log.info("Base de données déjà initialisée, passage de l'étape de seed.");
            return;
        }

        log.info("Initialisation de la base de données avec au minimum 5 enregistrements par table...");

        // 1. Conducteurs (5 enregistrements)
        Conducteur hamid = Conducteur.builder()
                .nom("El Amrani").prenom("Hamid").email("hamid.elamrani@covoiturage.ma")
                .telephone("0661123456").numeroPermis("PERMIS-1001-A").build();

        Conducteur youssef = Conducteur.builder()
                .nom("Benjelloun").prenom("Youssef").email("youssef.benjelloun@covoiturage.ma")
                .telephone("0662234567").numeroPermis("PERMIS-1002-B").build();

        Conducteur fatima = Conducteur.builder()
                .nom("Idrissi").prenom("Fatima Zahra").email("fatima.idrissi@covoiturage.ma")
                .telephone("0663345678").numeroPermis("PERMIS-1003-C").build();

        Conducteur omar = Conducteur.builder()
                .nom("Tazi").prenom("Omar").email("omar.tazi@covoiturage.ma")
                .telephone("0664456789").numeroPermis("PERMIS-1004-D").build();

        Conducteur sara = Conducteur.builder()
                .nom("Bennani").prenom("Sara").email("sara.bennani@covoiturage.ma")
                .telephone("0665567890").numeroPermis("PERMIS-1005-E").build();

        conducteurRepository.saveAll(List.of(hamid, youssef, fatima, omar, sara));

        // 2. Passagers (5 enregistrements)
        Passager karim = Passager.builder()
                .nom("Alami").prenom("Karim").email("karim.alami@covoiturage.ma").telephone("0671112233").build();

        Passager salma = Passager.builder()
                .nom("Chraibi").prenom("Salma").email("salma.chraibi@covoiturage.ma").telephone("0672223344").build();

        Passager imane = Passager.builder()
                .nom("Mansouri").prenom("Imane").email("imane.mansouri@covoiturage.ma").telephone("0673334455").build();

        Passager mehdi = Passager.builder()
                .nom("Alaoui").prenom("Mehdi").email("mehdi.alaoui@covoiturage.ma").telephone("0674445566").build();

        Passager noura = Passager.builder()
                .nom("Bouzidi").prenom("Noura").email("noura.bouzidi@covoiturage.ma").telephone("0675556677").build();

        passagerRepository.saveAll(List.of(karim, salma, imane, mehdi, noura));

        // 3. Véhicules (6 enregistrements)
        Vehicule vHamid = Vehicule.builder()
                .marque("Dacia").modele("Logan").immatriculation("12345-A-1").couleur("Gris Platine")
                .capacite(4).conducteur(hamid).build();

        Vehicule vYoussef = Vehicule.builder()
                .marque("Renault").modele("Clio 5").immatriculation("67890-B-6").couleur("Blanc Glacier")
                .capacite(4).conducteur(youssef).build();

        Vehicule vFatima = Vehicule.builder()
                .marque("Peugeot").modele("208").immatriculation("11223-D-26").couleur("Bleu Vertigo")
                .capacite(4).conducteur(fatima).build();

        Vehicule vOmar = Vehicule.builder()
                .marque("Volkswagen").modele("Golf 8").immatriculation("44556-E-1").couleur("Noir Intense")
                .capacite(4).conducteur(omar).build();

        Vehicule vSara = Vehicule.builder()
                .marque("Toyota").modele("Yaris").immatriculation("77889-A-6").couleur("Rouge Allure")
                .capacite(4).conducteur(sara).build();

        Vehicule vHamid2 = Vehicule.builder()
                .marque("Dacia").modele("Duster").immatriculation("99001-A-1").couleur("Orange Atacama")
                .capacite(4).conducteur(hamid).build();

        vehiculeRepository.saveAll(List.of(vHamid, vYoussef, vFatima, vOmar, vSara, vHamid2));

        // 4. Trajets (5 enregistrements dans le futur)
        LocalDateTime baseDate = LocalDateTime.now().plusDays(3).withHour(17).withMinute(0).withSecond(0).withNano(0);

        // Trajet 1 : Hamid (Rabat -> Marrakech avec étapes Casablanca et Settat, 3 places proposées)
        Trajet t1 = Trajet.builder()
                .conducteur(hamid).vehicule(vHamid).villeDepart("Rabat").villeArrivee("Marrakech")
                .dateDepart(baseDate).placesProposees(3).statut(StatutTrajet.PUBLIE).build();

        PointArret p1_0 = PointArret.builder().trajet(t1).ville("Rabat").ordre(0).heurePassage(baseDate).prixDepuisDepart(new BigDecimal("0.00")).build();
        PointArret p1_1 = PointArret.builder().trajet(t1).ville("Casablanca").ordre(1).heurePassage(baseDate.plusMinutes(75)).prixDepuisDepart(new BigDecimal("40.00")).build();
        PointArret p1_2 = PointArret.builder().trajet(t1).ville("Settat").ordre(2).heurePassage(baseDate.plusMinutes(135)).prixDepuisDepart(new BigDecimal("70.00")).build();
        PointArret p1_3 = PointArret.builder().trajet(t1).ville("Marrakech").ordre(3).heurePassage(baseDate.plusMinutes(270)).prixDepuisDepart(new BigDecimal("120.00")).build();
        t1.setPointsArret(List.of(p1_0, p1_1, p1_2, p1_3));

        // Trajet 2 : Youssef (Casablanca -> Tanger)
        LocalDateTime dateT2 = LocalDateTime.now().plusDays(4).withHour(8).withMinute(0).withSecond(0).withNano(0);
        Trajet t2 = Trajet.builder()
                .conducteur(youssef).vehicule(vYoussef).villeDepart("Casablanca").villeArrivee("Tanger")
                .dateDepart(dateT2).placesProposees(3).statut(StatutTrajet.PUBLIE).build();

        PointArret p2_0 = PointArret.builder().trajet(t2).ville("Casablanca").ordre(0).heurePassage(dateT2).prixDepuisDepart(new BigDecimal("0.00")).build();
        PointArret p2_1 = PointArret.builder().trajet(t2).ville("Rabat").ordre(1).heurePassage(dateT2.plusHours(1)).prixDepuisDepart(new BigDecimal("40.00")).build();
        PointArret p2_2 = PointArret.builder().trajet(t2).ville("Kenitra").ordre(2).heurePassage(dateT2.plusHours(2)).prixDepuisDepart(new BigDecimal("60.00")).build();
        PointArret p2_3 = PointArret.builder().trajet(t2).ville("Asilah").ordre(3).heurePassage(dateT2.plusHours(3).plusMinutes(30)).prixDepuisDepart(new BigDecimal("110.00")).build();
        PointArret p2_4 = PointArret.builder().trajet(t2).ville("Tanger").ordre(4).heurePassage(dateT2.plusHours(4).plusMinutes(15)).prixDepuisDepart(new BigDecimal("140.00")).build();
        t2.setPointsArret(List.of(p2_0, p2_1, p2_2, p2_3, p2_4));

        // Trajet 3 : Fatima (Fes -> Agadir)
        LocalDateTime dateT3 = LocalDateTime.now().plusDays(5).withHour(7).withMinute(0).withSecond(0).withNano(0);
        Trajet t3 = Trajet.builder()
                .conducteur(fatima).vehicule(vFatima).villeDepart("Fes").villeArrivee("Agadir")
                .dateDepart(dateT3).placesProposees(3).statut(StatutTrajet.PUBLIE).build();

        PointArret p3_0 = PointArret.builder().trajet(t3).ville("Fes").ordre(0).heurePassage(dateT3).prixDepuisDepart(new BigDecimal("0.00")).build();
        PointArret p3_1 = PointArret.builder().trajet(t3).ville("Meknes").ordre(1).heurePassage(dateT3.plusMinutes(45)).prixDepuisDepart(new BigDecimal("25.00")).build();
        PointArret p3_2 = PointArret.builder().trajet(t3).ville("Rabat").ordre(2).heurePassage(dateT3.plusHours(2).plusMinutes(30)).prixDepuisDepart(new BigDecimal("70.00")).build();
        PointArret p3_3 = PointArret.builder().trajet(t3).ville("Casablanca").ordre(3).heurePassage(dateT3.plusHours(3).plusMinutes(45)).prixDepuisDepart(new BigDecimal("100.00")).build();
        PointArret p3_4 = PointArret.builder().trajet(t3).ville("Agadir").ordre(4).heurePassage(dateT3.plusHours(8)).prixDepuisDepart(new BigDecimal("200.00")).build();
        t3.setPointsArret(List.of(p3_0, p3_1, p3_2, p3_3, p3_4));

        // Trajet 4 : Omar (Marrakech -> Oujda)
        LocalDateTime dateT4 = LocalDateTime.now().plusDays(6).withHour(6).withMinute(30).withSecond(0).withNano(0);
        Trajet t4 = Trajet.builder()
                .conducteur(omar).vehicule(vOmar).villeDepart("Marrakech").villeArrivee("Oujda")
                .dateDepart(dateT4).placesProposees(4).statut(StatutTrajet.PUBLIE).build();

        PointArret p4_0 = PointArret.builder().trajet(t4).ville("Marrakech").ordre(0).heurePassage(dateT4).prixDepuisDepart(new BigDecimal("0.00")).build();
        PointArret p4_1 = PointArret.builder().trajet(t4).ville("Casablanca").ordre(1).heurePassage(dateT4.plusHours(2).plusMinutes(45)).prixDepuisDepart(new BigDecimal("90.00")).build();
        PointArret p4_2 = PointArret.builder().trajet(t4).ville("Rabat").ordre(2).heurePassage(dateT4.plusHours(4)).prixDepuisDepart(new BigDecimal("130.00")).build();
        PointArret p4_3 = PointArret.builder().trajet(t4).ville("Fes").ordre(3).heurePassage(dateT4.plusHours(6).plusMinutes(30)).prixDepuisDepart(new BigDecimal("180.00")).build();
        PointArret p4_4 = PointArret.builder().trajet(t4).ville("Oujda").ordre(4).heurePassage(dateT4.plusHours(10)).prixDepuisDepart(new BigDecimal("250.00")).build();
        t4.setPointsArret(List.of(p4_0, p4_1, p4_2, p4_3, p4_4));

        // Trajet 5 : Sara (Tanger -> Casablanca)
        LocalDateTime dateT5 = LocalDateTime.now().plusDays(7).withHour(14).withMinute(0).withSecond(0).withNano(0);
        Trajet t5 = Trajet.builder()
                .conducteur(sara).vehicule(vSara).villeDepart("Tanger").villeArrivee("Casablanca")
                .dateDepart(dateT5).placesProposees(3).statut(StatutTrajet.PUBLIE).build();

        PointArret p5_0 = PointArret.builder().trajet(t5).ville("Tanger").ordre(0).heurePassage(dateT5).prixDepuisDepart(new BigDecimal("0.00")).build();
        PointArret p5_1 = PointArret.builder().trajet(t5).ville("Larache").ordre(1).heurePassage(dateT5.plusHours(1)).prixDepuisDepart(new BigDecimal("35.00")).build();
        PointArret p5_2 = PointArret.builder().trajet(t5).ville("Kenitra").ordre(2).heurePassage(dateT5.plusHours(2).plusMinutes(30)).prixDepuisDepart(new BigDecimal("75.00")).build();
        PointArret p5_3 = PointArret.builder().trajet(t5).ville("Rabat").ordre(3).heurePassage(dateT5.plusHours(3).plusMinutes(15)).prixDepuisDepart(new BigDecimal("100.00")).build();
        PointArret p5_4 = PointArret.builder().trajet(t5).ville("Casablanca").ordre(4).heurePassage(dateT5.plusHours(4).plusMinutes(30)).prixDepuisDepart(new BigDecimal("135.00")).build();
        t5.setPointsArret(List.of(p5_0, p5_1, p5_2, p5_3, p5_4));

        trajetRepository.saveAll(List.of(t1, t2, t3, t4, t5));

        // 5. Réservations (5 réservations réparties)
        // Réservation 1 : Salma sur le trajet 1 de Hamid (Settat -> Marrakech, 1 place, 50 DH)
        Reservation r1 = Reservation.builder()
                .passager(salma).trajet(t1).pointMontee(p1_2).pointDescente(p1_3)
                .nombrePlaces(1).montantTotal(new BigDecimal("50.00")).dateReservation(LocalDateTime.now())
                .statut(StatutReservation.CONFIRMEE).build();

        // Réservation 2 : Mehdi sur le trajet 2 de Youssef (Casablanca -> Kenitra, 1 place, 60 DH)
        Reservation r2 = Reservation.builder()
                .passager(mehdi).trajet(t2).pointMontee(p2_0).pointDescente(p2_2)
                .nombrePlaces(1).montantTotal(new BigDecimal("60.00")).dateReservation(LocalDateTime.now())
                .statut(StatutReservation.CONFIRMEE).build();

        // Réservation 3 : Noura sur le trajet 3 de Fatima (Fes -> Rabat, 2 places, 140 DH)
        Reservation r3 = Reservation.builder()
                .passager(noura).trajet(t3).pointMontee(p3_0).pointDescente(p3_2)
                .nombrePlaces(2).montantTotal(new BigDecimal("140.00")).dateReservation(LocalDateTime.now())
                .statut(StatutReservation.CONFIRMEE).build();

        // Réservation 4 : Karim sur le trajet 5 de Sara (Tanger -> Rabat, 1 place, 100 DH)
        Reservation r4 = Reservation.builder()
                .passager(karim).trajet(t5).pointMontee(p5_0).pointDescente(p5_3)
                .nombrePlaces(1).montantTotal(new BigDecimal("100.00")).dateReservation(LocalDateTime.now())
                .statut(StatutReservation.CONFIRMEE).build();

        // Réservation 5 : Imane sur le trajet 4 d'Omar (Marrakech -> Casablanca, 1 place, 90 DH)
        Reservation r5 = Reservation.builder()
                .passager(imane).trajet(t4).pointMontee(p4_0).pointDescente(p4_1)
                .nombrePlaces(1).montantTotal(new BigDecimal("90.00")).dateReservation(LocalDateTime.now())
                .statut(StatutReservation.CONFIRMEE).build();

        reservationRepository.saveAll(List.of(r1, r2, r3, r4, r5));

        log.info("Base de données initialisée avec succès : 5 conducteurs, 5 passagers, 6 véhicules, 5 trajets, 23 points d'arrêt, 5 réservations.");
    }
}

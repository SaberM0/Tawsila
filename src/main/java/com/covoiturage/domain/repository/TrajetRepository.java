package com.covoiturage.domain.repository;

import com.covoiturage.domain.entity.Trajet;
import com.covoiturage.dto.response.TrajetSearchResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrajetRepository extends JpaRepository<Trajet, Long> {

    @Query("SELECT DISTINCT t FROM Trajet t " +
           "JOIN FETCH t.conducteur " +
           "JOIN FETCH t.vehicule " +
           "LEFT JOIN FETCH t.pointsArret p " +
           "WHERE t.id = :id")
    Optional<Trajet> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT t FROM Trajet t " +
           "JOIN FETCH t.conducteur " +
           "JOIN FETCH t.vehicule " +
           "LEFT JOIN FETCH t.pointsArret p " +
           "WHERE t.conducteur.id = :conducteurId " +
           "ORDER BY t.dateDepart DESC")
    List<Trajet> findByConducteurIdWithDetails(@Param("conducteurId") Long conducteurId);

    List<Trajet> findByConducteurId(Long conducteurId);

    @Query(value = "SELECT new com.covoiturage.dto.response.TrajetSearchResult(" +
           "t, p1, p2, (p2.prixDepuisDepart - p1.prixDepuisDepart), " +
           "(SELECT COALESCE(SUM(r.nombrePlaces), 0) FROM Reservation r WHERE r.trajet.id = t.id AND r.statut = com.covoiturage.domain.enums.StatutReservation.CONFIRMEE)) " +
           "FROM Trajet t " +
           "JOIN t.pointsArret p1 " +
           "JOIN t.pointsArret p2 " +
           "WHERE t.statut = com.covoiturage.domain.enums.StatutTrajet.PUBLIE " +
           "AND LOWER(p1.ville) = LOWER(:villeDepart) " +
           "AND LOWER(p2.ville) = LOWER(:villeArrivee) " +
           "AND p1.ordre < p2.ordre " +
           "AND p1.heurePassage >= :startOfDay AND p1.heurePassage <= :endOfDay " +
           "AND (:maxPrix IS NULL OR (p2.prixDepuisDepart - p1.prixDepuisDepart) <= :maxPrix) " +
           "AND (t.placesProposees - (SELECT COALESCE(SUM(r2.nombrePlaces), 0) FROM Reservation r2 WHERE r2.trajet.id = t.id AND r2.statut = com.covoiturage.domain.enums.StatutReservation.CONFIRMEE)) >= :placesDemandees " +
           "ORDER BY p1.heurePassage ASC",
           countQuery = "SELECT COUNT(DISTINCT t) " +
           "FROM Trajet t " +
           "JOIN t.pointsArret p1 " +
           "JOIN t.pointsArret p2 " +
           "WHERE t.statut = com.covoiturage.domain.enums.StatutTrajet.PUBLIE " +
           "AND LOWER(p1.ville) = LOWER(:villeDepart) " +
           "AND LOWER(p2.ville) = LOWER(:villeArrivee) " +
           "AND p1.ordre < p2.ordre " +
           "AND p1.heurePassage >= :startOfDay AND p1.heurePassage <= :endOfDay " +
           "AND (:maxPrix IS NULL OR (p2.prixDepuisDepart - p1.prixDepuisDepart) <= :maxPrix) " +
           "AND (t.placesProposees - (SELECT COALESCE(SUM(r2.nombrePlaces), 0) FROM Reservation r2 WHERE r2.trajet.id = t.id AND r2.statut = com.covoiturage.domain.enums.StatutReservation.CONFIRMEE)) >= :placesDemandees")
    Page<TrajetSearchResult> searchTrajets(
            @Param("villeDepart") String villeDepart,
            @Param("villeArrivee") String villeArrivee,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("placesDemandees") int placesDemandees,
            @Param("maxPrix") BigDecimal maxPrix,
            Pageable pageable);
}

package com.covoiturage.domain.repository;

import com.covoiturage.domain.entity.Trajet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}

package com.covoiturage.domain.repository;

import com.covoiturage.domain.entity.Reservation;
import com.covoiturage.domain.enums.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT COALESCE(SUM(r.nombrePlaces), 0) FROM Reservation r " +
           "WHERE r.trajet.id = :trajetId AND r.statut = com.covoiturage.domain.enums.StatutReservation.CONFIRMEE")
    int countConfirmedSeatsByTrajetId(@Param("trajetId") Long trajetId);

    @Query("SELECT r FROM Reservation r " +
           "JOIN FETCH r.passager " +
           "JOIN FETCH r.trajet " +
           "JOIN FETCH r.pointMontee " +
           "JOIN FETCH r.pointDescente " +
           "WHERE r.trajet.id = :trajetId ORDER BY r.dateReservation DESC")
    List<Reservation> findByTrajetIdWithDetails(@Param("trajetId") Long trajetId);

    @Query("SELECT r FROM Reservation r " +
           "JOIN FETCH r.passager " +
           "JOIN FETCH r.trajet " +
           "JOIN FETCH r.pointMontee " +
           "JOIN FETCH r.pointDescente " +
           "WHERE r.passager.id = :passagerId ORDER BY r.dateReservation DESC")
    List<Reservation> findByPassagerIdWithDetails(@Param("passagerId") Long passagerId);

    boolean existsByTrajetIdAndPassagerIdAndStatut(Long trajetId, Long passagerId, StatutReservation statut);

    List<Reservation> findByTrajetId(Long trajetId);
}

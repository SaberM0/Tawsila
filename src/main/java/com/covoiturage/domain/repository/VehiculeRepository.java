package com.covoiturage.domain.repository;

import com.covoiturage.domain.entity.Vehicule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    boolean existsByImmatriculation(String immatriculation);

    boolean existsByImmatriculationAndIdNot(String immatriculation, Long id);

    Optional<Vehicule> findByImmatriculation(String immatriculation);

    List<Vehicule> findByConducteurId(Long conducteurId);

    @Query("SELECT COUNT(t) > 0 FROM Trajet t WHERE t.vehicule.id = :vehiculeId")
    boolean isVehiculeAssociatedWithTrajets(@Param("vehiculeId") Long vehiculeId);
}

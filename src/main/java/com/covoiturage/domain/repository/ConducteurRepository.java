package com.covoiturage.domain.repository;

import com.covoiturage.domain.entity.Conducteur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConducteurRepository extends JpaRepository<Conducteur, Long> {

    boolean existsByEmail(String email);

    boolean existsByNumeroPermis(String numeroPermis);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByNumeroPermisAndIdNot(String numeroPermis, Long id);

    Optional<Conducteur> findByEmail(String email);

    @Query("SELECT c FROM Conducteur c WHERE " +
           "LOWER(c.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.prenom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Conducteur> searchConducteurs(@Param("keyword") String keyword, Pageable pageable);
}

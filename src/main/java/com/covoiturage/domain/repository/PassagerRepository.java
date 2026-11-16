package com.covoiturage.domain.repository;

import com.covoiturage.domain.entity.Passager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PassagerRepository extends JpaRepository<Passager, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<Passager> findByEmail(String email);

    Page<Passager> findByEmailContainingIgnoreCase(String email, Pageable pageable);
}

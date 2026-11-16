package com.covoiturage.service.impl;

import com.covoiturage.domain.entity.Conducteur;
import com.covoiturage.domain.entity.Vehicule;
import com.covoiturage.domain.repository.ConducteurRepository;
import com.covoiturage.domain.repository.VehiculeRepository;
import com.covoiturage.dto.request.VehiculeCreateRequest;
import com.covoiturage.dto.request.VehiculeUpdateRequest;
import com.covoiturage.dto.response.VehiculeResponse;
import com.covoiturage.exception.BusinessConflictException;
import com.covoiturage.exception.BusinessValidationException;
import com.covoiturage.exception.ForbiddenOperationException;
import com.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.mapper.VehiculeMapper;
import com.covoiturage.service.VehiculeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehiculeServiceImpl implements VehiculeService {

    private static final Logger log = LoggerFactory.getLogger(VehiculeServiceImpl.class);

    private final VehiculeRepository vehiculeRepository;
    private final ConducteurRepository conducteurRepository;
    private final VehiculeMapper vehiculeMapper;

    @Override
    @Transactional
    public VehiculeResponse addVehicule(VehiculeCreateRequest request) {
        if (request.capacite() < 1 || request.capacite() > 8) {
            log.warn("Refus d'ajout de véhicule : capacité invalide {}", request.capacite());
            throw new BusinessValidationException("La capacité du véhicule doit être comprise entre 1 et 8 places");
        }

        if (vehiculeRepository.existsByImmatriculation(request.immatriculation())) {
            log.warn("Refus d'ajout de véhicule : immatriculation déjà existante {}", request.immatriculation());
            throw new BusinessConflictException("Un véhicule avec cette immatriculation existe déjà : " + request.immatriculation());
        }

        Conducteur conducteur = conducteurRepository.findById(request.conducteurId())
                .orElseThrow(() -> {
                    log.warn("Conducteur propriétaire non trouvé : id={}", request.conducteurId());
                    return new ResourceNotFoundException("Conducteur non trouvé avec l'identifiant : " + request.conducteurId());
                });

        Vehicule vehicule = vehiculeMapper.toEntity(request);
        vehicule.setConducteur(conducteur);

        Vehicule saved = vehiculeRepository.save(vehicule);
        log.info("Véhicule enregistré avec succès : id={}, immatriculation={}, conducteurId={}",
                saved.getId(), saved.getImmatriculation(), conducteur.getId());
        return vehiculeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public VehiculeResponse updateVehicule(Long vehiculeId, Long requestingDriverId, VehiculeUpdateRequest request) {
        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> {
                    log.warn("Véhicule introuvable pour mise à jour : id={}", vehiculeId);
                    return new ResourceNotFoundException("Véhicule non trouvé avec l'identifiant : " + vehiculeId);
                });

        if (requestingDriverId != null && !vehicule.getConducteur().getId().equals(requestingDriverId)) {
            log.warn("Action interdite : le conducteur {} a tenté de modifier le véhicule {} appartenant au conducteur {}",
                    requestingDriverId, vehiculeId, vehicule.getConducteur().getId());
            throw new ForbiddenOperationException("Vous ne pouvez modifier que vos propres véhicules");
        }

        if (request.capacite() < 1 || request.capacite() > 8) {
            log.warn("Refus de mise à jour véhicule : capacité invalide {}", request.capacite());
            throw new BusinessValidationException("La capacité du véhicule doit être comprise entre 1 et 8 places");
        }

        if (vehiculeRepository.existsByImmatriculationAndIdNot(request.immatriculation(), vehiculeId)) {
            log.warn("Refus de mise à jour véhicule : immatriculation déjà existante {}", request.immatriculation());
            throw new BusinessConflictException("Un véhicule avec cette immatriculation existe déjà : " + request.immatriculation());
        }

        vehiculeMapper.updateEntityFromRequest(request, vehicule);
        Vehicule updated = vehiculeRepository.save(vehicule);
        log.info("Véhicule mis à jour avec succès : id={}", updated.getId());
        return vehiculeMapper.toResponse(updated);
    }

    @Override
    public VehiculeResponse getVehiculeById(Long id) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Véhicule introuvable avec l'id : {}", id);
                    return new ResourceNotFoundException("Véhicule non trouvé avec l'identifiant : " + id);
                });
        return vehiculeMapper.toResponse(vehicule);
    }

    @Override
    public List<VehiculeResponse> getVehiculesByConducteur(Long conducteurId) {
        if (!conducteurRepository.existsById(conducteurId)) {
            log.warn("Conducteur introuvable lors de la recherche des véhicules : id={}", conducteurId);
            throw new ResourceNotFoundException("Conducteur non trouvé avec l'identifiant : " + conducteurId);
        }
        return vehiculeRepository.findByConducteurId(conducteurId).stream()
                .map(vehiculeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteVehicule(Long vehiculeId, Long requestingDriverId) {
        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> {
                    log.warn("Véhicule introuvable pour suppression : id={}", vehiculeId);
                    return new ResourceNotFoundException("Véhicule non trouvé avec l'identifiant : " + vehiculeId);
                });

        if (requestingDriverId != null && !vehicule.getConducteur().getId().equals(requestingDriverId)) {
            log.warn("Action interdite : conducteur {} a tenté de supprimer le véhicule {} du conducteur {}",
                    requestingDriverId, vehiculeId, vehicule.getConducteur().getId());
            throw new ForbiddenOperationException("Vous ne pouvez supprimer que vos propres véhicules");
        }

        if (vehiculeRepository.isVehiculeAssociatedWithTrajets(vehiculeId)) {
            log.warn("Refus de suppression : le véhicule {} est associé à des trajets", vehiculeId);
            throw new BusinessConflictException("Impossible de supprimer un véhicule lorsqu'il est associé à un ou plusieurs trajets");
        }

        vehiculeRepository.delete(vehicule);
        log.info("Véhicule supprimé avec succès : id={}", vehiculeId);
    }
}

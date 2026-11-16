package com.covoiturage.service.impl;

import com.covoiturage.domain.entity.Conducteur;
import com.covoiturage.domain.repository.ConducteurRepository;
import com.covoiturage.dto.request.ConducteurCreateRequest;
import com.covoiturage.dto.request.ConducteurUpdateRequest;
import com.covoiturage.dto.response.ConducteurResponse;
import com.covoiturage.exception.BusinessConflictException;
import com.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.mapper.ConducteurMapper;
import com.covoiturage.service.ConducteurService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConducteurServiceImpl implements ConducteurService {

    private static final Logger log = LoggerFactory.getLogger(ConducteurServiceImpl.class);

    private final ConducteurRepository conducteurRepository;
    private final ConducteurMapper conducteurMapper;

    @Override
    @Transactional
    public ConducteurResponse createConducteur(ConducteurCreateRequest request) {
        if (conducteurRepository.existsByEmail(request.email())) {
            log.warn("Refus de création de conducteur : email déjà utilisé {}", request.email());
            throw new BusinessConflictException("Un conducteur avec cet email existe déjà : " + request.email());
        }

        if (conducteurRepository.existsByNumeroPermis(request.numeroPermis())) {
            log.warn("Refus de création de conducteur : permis déjà utilisé {}", request.numeroPermis());
            throw new BusinessConflictException("Un conducteur avec ce numéro de permis existe déjà : " + request.numeroPermis());
        }

        Conducteur conducteur = conducteurMapper.toEntity(request);
        Conducteur saved = conducteurRepository.save(conducteur);
        log.info("Conducteur créé avec succès : id={}, email={}", saved.getId(), saved.getEmail());
        return conducteurMapper.toResponse(saved);
    }

    @Override
    public ConducteurResponse getConducteurById(Long id) {
        Conducteur conducteur = conducteurRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Conducteur introuvable avec l'id : {}", id);
                    return new ResourceNotFoundException("Conducteur non trouvé avec l'identifiant : " + id);
                });
        return conducteurMapper.toResponse(conducteur);
    }

    @Override
    public Page<ConducteurResponse> getAllConducteurs(Pageable pageable) {
        return conducteurRepository.findAll(pageable)
                .map(conducteurMapper::toResponse);
    }

    @Override
    @Transactional
    public ConducteurResponse updateConducteur(Long id, ConducteurUpdateRequest request) {
        Conducteur conducteur = conducteurRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Tentative de mise à jour d'un conducteur inexistant avec l'id : {}", id);
                    return new ResourceNotFoundException("Conducteur non trouvé avec l'identifiant : " + id);
                });

        if (conducteurRepository.existsByEmailAndIdNot(request.email(), id)) {
            log.warn("Refus de mise à jour conducteur : email déjà utilisé par un autre compte {}", request.email());
            throw new BusinessConflictException("Un conducteur avec cet email existe déjà : " + request.email());
        }

        if (conducteurRepository.existsByNumeroPermisAndIdNot(request.numeroPermis(), id)) {
            log.warn("Refus de mise à jour conducteur : permis déjà utilisé par un autre compte {}", request.numeroPermis());
            throw new BusinessConflictException("Un conducteur avec ce numéro de permis existe déjà : " + request.numeroPermis());
        }

        conducteurMapper.updateEntityFromRequest(request, conducteur);
        Conducteur updated = conducteurRepository.save(conducteur);
        log.info("Conducteur mis à jour avec succès : id={}", updated.getId());
        return conducteurMapper.toResponse(updated);
    }

    @Override
    public Page<ConducteurResponse> searchConducteurs(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllConducteurs(pageable);
        }
        return conducteurRepository.searchConducteurs(keyword.trim(), pageable)
                .map(conducteurMapper::toResponse);
    }
}

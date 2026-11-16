package com.covoiturage.service.impl;

import com.covoiturage.domain.entity.Passager;
import com.covoiturage.domain.repository.PassagerRepository;
import com.covoiturage.dto.request.PassagerCreateRequest;
import com.covoiturage.dto.request.PassagerUpdateRequest;
import com.covoiturage.dto.response.PassagerResponse;
import com.covoiturage.exception.BusinessConflictException;
import com.covoiturage.exception.ResourceNotFoundException;
import com.covoiturage.mapper.PassagerMapper;
import com.covoiturage.service.PassagerService;
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
public class PassagerServiceImpl implements PassagerService {

    private static final Logger log = LoggerFactory.getLogger(PassagerServiceImpl.class);

    private final PassagerRepository passagerRepository;
    private final PassagerMapper passagerMapper;

    @Override
    @Transactional
    public PassagerResponse createPassager(PassagerCreateRequest request) {
        if (passagerRepository.existsByEmail(request.email())) {
            log.warn("Refus de création de passager : email déjà existant {}", request.email());
            throw new BusinessConflictException("Un passager avec cet email existe déjà : " + request.email());
        }

        Passager passager = passagerMapper.toEntity(request);
        Passager saved = passagerRepository.save(passager);
        log.info("Passager créé avec succès : id={}, email={}", saved.getId(), saved.getEmail());
        return passagerMapper.toResponse(saved);
    }

    @Override
    public PassagerResponse getPassagerById(Long id) {
        Passager passager = passagerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Passager introuvable avec l'id : {}", id);
                    return new ResourceNotFoundException("Passager non trouvé avec l'identifiant : " + id);
                });
        return passagerMapper.toResponse(passager);
    }

    @Override
    public Page<PassagerResponse> getAllPassagers(Pageable pageable) {
        return passagerRepository.findAll(pageable)
                .map(passagerMapper::toResponse);
    }

    @Override
    @Transactional
    public PassagerResponse updatePassager(Long id, PassagerUpdateRequest request) {
        Passager passager = passagerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Tentative de mise à jour d'un passager inexistant : id={}", id);
                    return new ResourceNotFoundException("Passager non trouvé avec l'identifiant : " + id);
                });

        if (passagerRepository.existsByEmailAndIdNot(request.email(), id)) {
            log.warn("Refus de mise à jour passager : email déjà pris par un autre {}", request.email());
            throw new BusinessConflictException("Un passager avec cet email existe déjà : " + request.email());
        }

        passagerMapper.updateEntityFromRequest(request, passager);
        Passager updated = passagerRepository.save(passager);
        log.info("Passager mis à jour avec succès : id={}", updated.getId());
        return passagerMapper.toResponse(updated);
    }

    @Override
    public PassagerResponse getPassagerByEmail(String email) {
        Passager passager = passagerRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Passager introuvable avec l'email : {}", email);
                    return new ResourceNotFoundException("Passager non trouvé avec l'email : " + email);
                });
        return passagerMapper.toResponse(passager);
    }

    @Override
    public Page<PassagerResponse> searchPassagersByEmail(String email, Pageable pageable) {
        if (email == null || email.trim().isEmpty()) {
            return getAllPassagers(pageable);
        }
        return passagerRepository.findByEmailContainingIgnoreCase(email.trim(), pageable)
                .map(passagerMapper::toResponse);
    }
}

package com.covoiturage.service;

import com.covoiturage.dto.request.ConducteurCreateRequest;
import com.covoiturage.dto.request.ConducteurUpdateRequest;
import com.covoiturage.dto.response.ConducteurResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConducteurService {

    ConducteurResponse createConducteur(ConducteurCreateRequest request);

    ConducteurResponse getConducteurById(Long id);

    Page<ConducteurResponse> getAllConducteurs(Pageable pageable);

    ConducteurResponse updateConducteur(Long id, ConducteurUpdateRequest request);

    Page<ConducteurResponse> searchConducteurs(String keyword, Pageable pageable);
}

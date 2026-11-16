package com.covoiturage.service;

import com.covoiturage.dto.request.PassagerCreateRequest;
import com.covoiturage.dto.request.PassagerUpdateRequest;
import com.covoiturage.dto.response.PassagerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PassagerService {

    PassagerResponse createPassager(PassagerCreateRequest request);

    PassagerResponse getPassagerById(Long id);

    Page<PassagerResponse> getAllPassagers(Pageable pageable);

    PassagerResponse updatePassager(Long id, PassagerUpdateRequest request);

    PassagerResponse getPassagerByEmail(String email);

    Page<PassagerResponse> searchPassagersByEmail(String email, Pageable pageable);
}

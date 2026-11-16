package com.covoiturage.service;

import com.covoiturage.dto.request.VehiculeCreateRequest;
import com.covoiturage.dto.request.VehiculeUpdateRequest;
import com.covoiturage.dto.response.VehiculeResponse;

import java.util.List;

public interface VehiculeService {

    VehiculeResponse addVehicule(VehiculeCreateRequest request);

    VehiculeResponse updateVehicule(Long vehiculeId, Long requestingDriverId, VehiculeUpdateRequest request);

    VehiculeResponse getVehiculeById(Long id);

    List<VehiculeResponse> getVehiculesByConducteur(Long conducteurId);

    void deleteVehicule(Long vehiculeId, Long requestingDriverId);
}

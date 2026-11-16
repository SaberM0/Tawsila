package com.covoiturage.mapper;

import com.covoiturage.domain.entity.Conducteur;
import com.covoiturage.dto.request.ConducteurCreateRequest;
import com.covoiturage.dto.request.ConducteurUpdateRequest;
import com.covoiturage.dto.response.ConducteurResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ConducteurMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicules", ignore = true)
    @Mapping(target = "trajets", ignore = true)
    Conducteur toEntity(ConducteurCreateRequest request);

    ConducteurResponse toResponse(Conducteur entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicules", ignore = true)
    @Mapping(target = "trajets", ignore = true)
    void updateEntityFromRequest(ConducteurUpdateRequest request, @MappingTarget Conducteur entity);
}

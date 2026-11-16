package com.covoiturage.mapper;

import com.covoiturage.domain.entity.Passager;
import com.covoiturage.dto.request.PassagerCreateRequest;
import com.covoiturage.dto.request.PassagerUpdateRequest;
import com.covoiturage.dto.response.PassagerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PassagerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    Passager toEntity(PassagerCreateRequest request);

    PassagerResponse toResponse(Passager entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    void updateEntityFromRequest(PassagerUpdateRequest request, @MappingTarget Passager entity);
}

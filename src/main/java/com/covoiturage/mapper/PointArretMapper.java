package com.covoiturage.mapper;

import com.covoiturage.domain.entity.PointArret;
import com.covoiturage.dto.request.PointArretRequest;
import com.covoiturage.dto.response.PointArretResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PointArretMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trajet", ignore = true)
    PointArret toEntity(PointArretRequest request);

    PointArretResponse toResponse(PointArret entity);

    List<PointArretResponse> toResponseList(List<PointArret> entities);
}

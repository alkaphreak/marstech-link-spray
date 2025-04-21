package fr.marstech.mtlinkspray.mapper;

import fr.marstech.mtlinkspray.dto.DashboardDto;
import fr.marstech.mtlinkspray.entity.DashboardEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DashboardMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "items", source = "items")
    DashboardDto toDto(DashboardEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "items", source = "items")
    DashboardEntity toEntity(DashboardDto dto);
}

package io.github.gabrielpetry23.logscopeapi.controller.mapper;

import io.github.gabrielpetry23.logscopeapi.dto.LogRequestDTO;
import io.github.gabrielpetry23.logscopeapi.dto.LogResponseDTO;
import io.github.gabrielpetry23.logscopeapi.model.Log;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LogMapper {
    Log toEntity(LogRequestDTO dto);

    LogResponseDTO toDTO(Log log);
}

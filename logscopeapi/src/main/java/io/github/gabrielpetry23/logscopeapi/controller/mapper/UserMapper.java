package io.github.gabrielpetry23.logscopeapi.controller.mapper;

import io.github.gabrielpetry23.logscopeapi.dto.UserCreationRequestDTO;
import io.github.gabrielpetry23.logscopeapi.dto.UserResponseDTO;
import io.github.gabrielpetry23.logscopeapi.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserCreationRequestDTO dto);

    UserResponseDTO toDTO(User user);
}

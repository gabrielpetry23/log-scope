package io.github.gabrielpetry23.logscopeapi.controller.mapper;

import io.github.gabrielpetry23.logscopeapi.dto.AlertRuleDTO;
import io.github.gabrielpetry23.logscopeapi.model.AlertRule;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AlertRuleMapper {
    AlertRuleDTO toDTO(AlertRule entity);
    AlertRule toEntity(AlertRuleDTO dto);
}

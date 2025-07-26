package io.github.gabrielpetry23.logscopeapi.controller;

import io.github.gabrielpetry23.logscopeapi.dto.AlertRuleDTO;
import io.github.gabrielpetry23.logscopeapi.service.AlertRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
public class AlertRuleController {

    private final AlertRuleService alertRuleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'GLOBAL_ADMIN')")
    public ResponseEntity<List<AlertRuleDTO>> getUserRules(@AuthenticationPrincipal UserDetails user) {
        List<AlertRuleDTO> rules = alertRuleService.getUserAlertRules(user.getUsername());
        return ResponseEntity.ok(rules);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'GLOBAL_ADMIN')")
    public ResponseEntity<AlertRuleDTO> create(@Valid @RequestBody AlertRuleDTO dto, @AuthenticationPrincipal UserDetails user) {
        AlertRuleDTO createdRule = alertRuleService.createAlertRule(dto, user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'GLOBAL_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id, @AuthenticationPrincipal UserDetails user) {
        alertRuleService.deleteAlertRule(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'GLOBAL_ADMIN')")
    public ResponseEntity<AlertRuleDTO> update(@PathVariable String id, @Valid @RequestBody AlertRuleDTO dto, @AuthenticationPrincipal UserDetails user) {
        AlertRuleDTO updatedRule = alertRuleService.updateAlertRule(id, dto, user.getUsername());
        return ResponseEntity.ok(updatedRule);
    }
}

package io.github.gabrielpetry23.logscopeapi.dto;

import java.util.List;

public record AlertTestResponseDTO(
        boolean wouldTrigger,
        String matchedRule,
        List<String> allMatchedRules
) {
    // Constructor for backward compatibility
    public AlertTestResponseDTO(boolean wouldTrigger, String matchedRule) {
        this(wouldTrigger, matchedRule, matchedRule != null ? List.of(matchedRule) : List.of());
    }
}

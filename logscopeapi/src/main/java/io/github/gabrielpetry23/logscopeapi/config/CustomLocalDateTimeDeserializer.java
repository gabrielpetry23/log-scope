package io.github.gabrielpetry23.logscopeapi.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CustomLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Try parsing as LocalDateTime first (ISO_LOCAL_DATE_TIME)
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e1) {
            try {
                // Try parsing as OffsetDateTime and convert to LocalDateTime
                return OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime();
            } catch (DateTimeParseException e2) {
                try {
                    // Try parsing as ZonedDateTime and convert to LocalDateTime
                    return ZonedDateTime.parse(value, DateTimeFormatter.ISO_ZONED_DATE_TIME).toLocalDateTime();
                } catch (DateTimeParseException e3) {
                    try {
                        // Try parsing with common formats
                        return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                    } catch (DateTimeParseException e4) {
                        // If all else fails, use current time
                        return LocalDateTime.now();
                    }
                }
            }
        }
    }
}

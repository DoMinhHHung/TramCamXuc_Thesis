package iuh.fit.se.tramcamxuc.modules.subscription.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.se.tramcamxuc.modules.subscription.model.PlanFeatures;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Converter(autoApply = true)
@Slf4j
@RequiredArgsConstructor
public class PlanFeaturesConverter implements AttributeConverter<PlanFeatures, String> {

    private final ObjectMapper objectMapper;

    @Override
    public String convertToDatabaseColumn(PlanFeatures attribute) {
        if (attribute == null) return null;
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Lỗi convert PlanFeatures sang JSON", e);
            throw new RuntimeException("JSON writing error");
        }
    }

    @Override
    public PlanFeatures convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return new PlanFeatures();
        try {
            return objectMapper.readValue(dbData, PlanFeatures.class);
        } catch (JsonProcessingException e) {
            log.error("Lỗi convert JSON sang PlanFeatures", e);
            return new PlanFeatures();
        }
    }
}
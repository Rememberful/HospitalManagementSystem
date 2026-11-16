package com.hms.hospital.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.List;

@Converter
public class BillingItemsConverter implements AttributeConverter<List<BillingItem>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<BillingItem> attribute) {
        try {
            return MAPPER.writeValueAsString(attribute == null ? Collections.emptyList() : attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialize billing items", e);
        }
    }

    @Override
    public List<BillingItem> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) return Collections.emptyList();
            return MAPPER.readValue(dbData, new TypeReference<List<BillingItem>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Unable to deserialize billing items", e);
        }
    }
}

package com.cabybara.prolearningplatform.converter;

import com.cabybara.prolearningplatform.model.ResourceRef;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter
public class ResourceRefListConverter implements AttributeConverter<List<ResourceRef>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<ResourceRef>> TYPE_REF = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<ResourceRef> refs) {
        if (refs == null || refs.isEmpty()) return null;
        try {
            return MAPPER.writeValueAsString(refs);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<ResourceRef> convertToEntityAttribute(String data) {
        if (data == null || data.isBlank()) return new ArrayList<>();
        try {
            return MAPPER.readValue(data, TYPE_REF);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}

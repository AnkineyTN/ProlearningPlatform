package com.cabybara.prolearningplatform.mapper;

import com.cabybara.prolearningplatform.model.GoogleCredential;
import com.google.api.client.auth.oauth2.StoredCredential;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface GoogleAuthItemMapper {
    @Mapping(source = "accessToken", target = "accessToken")
    @Mapping(source = "refreshToken", target = "refreshToken")
    @Mapping(source = "expirationTimeMilliseconds", target = "expiresAt",
            qualifiedByName = "milisecToOffsetDateTime")
    GoogleCredential fromStoreCredential(StoredCredential value);

    @Named("milisecToOffsetDateTime")
    default OffsetDateTime milisecToOffsetDateTime(Long milisec) {
        Instant instant = Instant.ofEpochMilli(milisec);
        return instant.atOffset(ZoneOffset.UTC);
    }

    @Mapping(source = "accessToken", target = "accessToken")
    @Mapping(source = "refreshToken", target = "refreshToken")
    @Mapping(source = "expiresAt", target = "expirationTimeMilliseconds",
            qualifiedByName = "offsetDateTimeToMillisec")
    StoredCredential toStoreCredential(GoogleCredential entity);

    @Named("offsetDateTimeToMillisec")
    default Long offsetDateTimeToMillisec(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return offsetDateTime.toInstant().toEpochMilli();
    }
}

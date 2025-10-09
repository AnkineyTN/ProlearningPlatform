package com.cabybara.prolearningplatform.dto;

import com.google.api.services.oauth2.Oauth2;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GoogleUserInfoDto {
    private String sub;
    private String email;
    private Boolean emailVerified;
    private String name;
    private String picture;
    private String givenName;
    private String familyName;
}
package com.cabybara.prolearningplatform.dto.response.user;

import com.cabybara.prolearningplatform.model.User;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSearchResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;

    public static UserSearchResponse from(User user) {
        return new UserSearchResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail()
        );
    }
}

package com.cabybara.prolearningplatform.service.user;

import com.cabybara.prolearningplatform.dto.request.ChangePasswordRequestDto;
import com.cabybara.prolearningplatform.dto.GoogleUserInfoDto;
import com.cabybara.prolearningplatform.dto.request.RegisterRequestDto;
import com.cabybara.prolearningplatform.dto.response.UserResponseDto;
import com.cabybara.prolearningplatform.enums.Role;
import com.cabybara.prolearningplatform.model.User;
import com.google.api.services.oauth2.model.Userinfo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    UserDetails loadUserByUsername(String username);

    UserResponseDto loadUserByEmail(String email);

    UserResponseDto addUser(RegisterRequestDto registerRequestDto, Role role) throws Exception;

    void deleteUser(String email);

    void updateUserPassword(String email, ChangePasswordRequestDto changePasswordRequestDto);

    User findOrCreateFromGoogle(Userinfo userInfo);

    User findOrCreateFromGoogle(GoogleUserInfoDto googleUserInfoDto);
}

package com.cabybara.prolearningplatform.service.user;

import com.cabybara.prolearningplatform.dto.request.user.ChangePasswordRequestDto;
import com.cabybara.prolearningplatform.dto.helper.GoogleUserInfoDto;
import com.cabybara.prolearningplatform.dto.request.user.RegisterRequestDto;
import com.cabybara.prolearningplatform.dto.request.user.UserUpdatingRequestDto;
import com.cabybara.prolearningplatform.dto.response.user.UserResponseDto;
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

    UserResponseDto updateUser(Long userId, UserUpdatingRequestDto userUpdatingRequestDto);

    void updateUserPassword(String email, ChangePasswordRequestDto changePasswordRequestDto);

    User findOrCreateFromGoogle(Userinfo userInfo);

    User findOrCreateFromGoogle(GoogleUserInfoDto googleUserInfoDto);

    User getUserById(Long userId);
}

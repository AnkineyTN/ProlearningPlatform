package com.cabybara.prolearningplatform.service;

import com.cabybara.prolearningplatform.dto.ChangePasswordRequestDto;
import com.cabybara.prolearningplatform.dto.RegisterRequestDto;
import com.cabybara.prolearningplatform.dto.UserResponseDto;
import com.cabybara.prolearningplatform.enums.Role;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    UserDetails loadUserByUsername(String username);

    UserResponseDto loadUserByEmail(String email);

    UserResponseDto addUser(RegisterRequestDto registerRequestDto, Role role) throws Exception;

    void deleteUser(String email);

    void updateUserPassword(String email, ChangePasswordRequestDto changePasswordRequestDto);
}

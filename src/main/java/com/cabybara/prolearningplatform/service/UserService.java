package com.cabybara.prolearningplatform.service;

import com.cabybara.prolearningplatform.dto.RegisterUserDto;
import com.cabybara.prolearningplatform.dto.UserResponseDto;
import com.cabybara.prolearningplatform.enums.Role;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

public interface UserService extends UserDetailsService {
    UserDetails loadUserByUsername(String username);

    UserResponseDto addUser(RegisterUserDto registerUserDto, Role role) throws Exception;
}

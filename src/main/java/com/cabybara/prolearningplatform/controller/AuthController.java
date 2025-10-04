package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.RegisterUserDto;
import com.cabybara.prolearningplatform.enums.Role;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public User registerNormalUser(@RequestBody RegisterUserDto registerUserDto) throws Exception {
        User user = userService.addUser(registerUserDto, Role.ROLE_USER);
        return user;
    }

    @PostMapping("/register/admin")
    public User registerAdminUser(@RequestBody RegisterUserDto registerUserDto) throws Exception {
        User user = userService.addUser(registerUserDto, Role.ROLE_ADMIN);
        return user;
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginUserDto loginUserDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginUserDto.getUsername(), loginUserDto.getPassword())
        );

        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(authentication);
        } else {
            return "Login failed!";
        }
    }

}

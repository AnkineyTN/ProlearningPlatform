package com.cabybara.prolearningplatform.service;

import java.util.HashSet;
import java.util.Optional;

import com.cabybara.prolearningplatform.dto.RegisterUserDto;
import com.cabybara.prolearningplatform.enums.Role;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
//    private final EmailService emailService;
    private final JwtService jwtService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        return user.orElseThrow(() -> new UsernameNotFoundException("User with username: " + username + " not found!"));
    }

    public User addUser(RegisterUserDto registerUserDto, Role role) throws Exception {
        User existedUser = userRepository.findByEmail(registerUserDto.getEmail()).orElseGet(() -> null);
        if (existedUser != null) {
            if (!existedUser.isEnabled()) {
                String verificationCode = jwtService.generateToken(registerUserDto.getEmail());
                existedUser.setVerificationCode(verificationCode);
                emailService.sendVerificationEmail(registerUserDto.getEmail(), verificationCode);
                return userRepository.save(existedUser);
            }
            throw new Exception("User has existed!");
        }

        User newUser = User.builder()
                .username(registerUserDto.getUsername())
                .password(passwordEncoder.encode(registerUserDto.getPassword()))
                .email(registerUserDto.getEmail())
                .enabled(false)
                .roles(new HashSet<>())
                .build();

        Authority defaultAuthority = Authority.builder()
                .user(newUser)
                .authority(role)
                .build();

        newUser.getRoles().add(defaultAuthority);
        String verificationCode = jwtService.generateToken(registerUserDto.getEmail());
        emailService.sendVerificationEmail(registerUserDto.getEmail(), verificationCode);

        return userRepository.save(newUser);
    }
}

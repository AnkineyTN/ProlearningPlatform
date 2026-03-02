package com.cabybara.prolearningplatform.mapper;

import com.cabybara.prolearningplatform.dto.request.user.UserUpdatingRequestDto;
import com.cabybara.prolearningplatform.dto.response.user.UserResponseDto;
import com.cabybara.prolearningplatform.enums.Role;
import com.cabybara.prolearningplatform.model.Authority;
import com.cabybara.prolearningplatform.model.User;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "roles", target = "roles", qualifiedByName = "mapAuthoritiesToStrings")
    UserResponseDto toUserResponseDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(UserUpdatingRequestDto userUpdatingRequestDto, @MappingTarget User user);

    @Named("mapAuthoritiesToStrings")
    default Set<Role> mapAuthoritiesToStrings(Set<Authority> authorities) {
        if (authorities == null) {
            return null;
        }
        return authorities.stream()
                .map(Authority::getAuthority)
                .collect(Collectors.toSet());
    }
}

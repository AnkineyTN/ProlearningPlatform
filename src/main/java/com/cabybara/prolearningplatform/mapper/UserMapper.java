package com.cabybara.prolearningplatform.mapper;

import com.cabybara.prolearningplatform.dto.request.UpdateUserRequestDto;
import com.cabybara.prolearningplatform.dto.response.UserResponseDto;
import com.cabybara.prolearningplatform.enums.Role;
import com.cabybara.prolearningplatform.model.Authority;
import com.cabybara.prolearningplatform.model.User;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "roles", target = "roles", qualifiedByName = "mapAuthoritiesToStrings")
    UserResponseDto toUserResponseDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(UpdateUserRequestDto updateUserRequestDto, @MappingTarget User user);

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

package com.cabybara.prolearningplatform.service.admin;

import com.cabybara.prolearningplatform.dto.request.admin.AdminUserUpdateRequestDto;
import com.cabybara.prolearningplatform.dto.response.admin.AdminUserListItemResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserManagementService {

    Page<AdminUserListItemResponseDto> listUsers(Pageable pageable);

    UserResponseDto updateUser(Long userId, AdminUserUpdateRequestDto request);

    void deleteUser(Long userId);
}

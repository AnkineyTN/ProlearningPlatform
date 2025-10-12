package com.cabybara.prolearningplatform.service;

import com.cabybara.prolearningplatform.dto.SetCreationRequestDto;
import com.cabybara.prolearningplatform.dto.SetResponseDto;
import com.cabybara.prolearningplatform.dto.SetUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SetService {

    Page<SetResponseDto> getAllSet(Long id, Pageable pageable);

    SetResponseDto createSet(Long userId, SetCreationRequestDto setCreationRequestDto);

    SetResponseDto updateSet(Long setId, Long userId, SetUpdateRequestDto setUpdateRequestDto);

    void deleteSet(Long setId, Long userId);
}

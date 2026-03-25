package com.cabybara.prolearningplatform.service.set;

import com.cabybara.prolearningplatform.dto.request.set.SetCreationRequestDto;
import com.cabybara.prolearningplatform.dto.response.set.SetResponseDto;
import com.cabybara.prolearningplatform.dto.request.set.SetUpdatingRequestDto;
import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.model.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SetService {

    Page<SetResponseDto> getAllSet(String q, Privacy privacy, Pageable pageable);

    SetResponseDto createSet(Long userId, SetCreationRequestDto setCreationRequestDto);

    SetResponseDto updateSet(Long setId, Long userId, SetUpdatingRequestDto setUpdatingRequestDto);

    void deleteSet(Long setId, Long userId);

    Set getSetById(Long setId);

    SetResponseDto getSet(Long setId);
}

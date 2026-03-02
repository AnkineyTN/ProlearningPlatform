package com.cabybara.prolearningplatform.service.set.impl;

import com.cabybara.prolearningplatform.dto.request.set.SetUpdatingRequestDto;
import com.cabybara.prolearningplatform.dto.request.set.SetCreationRequestDto;
import com.cabybara.prolearningplatform.dto.response.set.SetResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.cabybara.prolearningplatform.exception.ResourceAlreadyExistsException;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.mapper.SetMapper;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.repository.SetRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.set.SetService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SetServiceImpl implements SetService {
    private final SetRepository setRepository;
    private final UserRepository userRepository;
    private final SetMapper setMapper;

    @Override
    public Set getSetById(Long setId) {
        return setRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Set with id " + setId + " not found"));
    }

    @Override
    public Page<SetResponseDto> getAllSet(Long userId, Pageable pageable) {
        Page<Set> allSetPages = setRepository.findAllByUserId(userId, pageable);

        return allSetPages.map(setMapper::toSetResponseDto);
    }

    @Override
    public SetResponseDto getSet(Long setId) {
        Set set = setRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Set with id " + setId + " not found"));

        return setMapper.toSetResponseDto(set);
    }

    @Override
    @Transactional
    public SetResponseDto createSet(Long userId, SetCreationRequestDto setCreationRequestDto) {
        if (setRepository.existsByTitle(setCreationRequestDto.getTitle())) {
            throw new ResourceAlreadyExistsException("Set with title '" + setCreationRequestDto.getTitle() + "' already exists.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id: " + userId + " not found!"));

        Set newSet = setMapper.fromSetCreationRequestDto(setCreationRequestDto);
        newSet.setUser(user);
        newSet.setNotes(new ArrayList<>());
        newSet.setFlashcards(new ArrayList<>());

        Set savedSet = setRepository.save(newSet);
        return setMapper.toSetResponseDto(savedSet);
    }

    @Override
    public SetResponseDto updateSet(Long setId, Long userId, SetUpdatingRequestDto setUpdatingRequestDto) {
        Set set = setRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Set with id: " + setId + " not found!"));

        if (set.getUser().getId() != userId) {
            throw new AccessDeniedException("You are not allowed to update this set.");
        }

        if (setUpdatingRequestDto.getTitle() != null && setRepository.existsByTitleAndIdNot(setUpdatingRequestDto.getTitle(), setId)) {
            throw new ResourceAlreadyExistsException("Another Set with title '" + setUpdatingRequestDto.getTitle() + "' already exists.");
        }

        setMapper.updateSetFromDto(setUpdatingRequestDto, set);
        Set updatedSet = setRepository.save(set);

        return setMapper.toSetResponseDto(updatedSet);
    }

    @Override
    public void deleteSet(Long setId, Long userId) {
        Set set = setRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Set with id: " + setId + " not found!"));

        if (!Objects.equals(set.getUser().getId(), userId)) {
            throw new AccessDeniedException("You are not allowed to delete this set.");
        }

        setRepository.delete(set);
    }
}

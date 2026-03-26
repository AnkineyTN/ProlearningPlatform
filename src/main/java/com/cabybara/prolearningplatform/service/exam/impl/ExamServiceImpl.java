package com.cabybara.prolearningplatform.service.exam.impl;

import com.cabybara.prolearningplatform.dto.request.exam.CreateExamRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.GenerateExamByFileRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.GenerateExamByNoteRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.UpdateExamRequestDto;
import com.cabybara.prolearningplatform.dto.response.exam.ExamResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.GenerateExamByAIResponseDto;
import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.exception.ResourceAlreadyExistsException;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.mapper.ExamMapper;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.exam.Exam;
import com.cabybara.prolearningplatform.model.note.Note;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.repository.ExamRepository;
import com.cabybara.prolearningplatform.repository.SetRepository;
import com.cabybara.prolearningplatform.service.ai.AIExamService;
import com.cabybara.prolearningplatform.service.exam.ExamService;
import com.cabybara.prolearningplatform.service.file.FileService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {
    private final AuthenticationContext authenticationContext;
    private final ExamRepository examRepository;
    private final SetRepository setRepository;
    private final NoteRepository noteRepository;
    private final ExamMapper examMapper;
    private final FileService fileService;
    private final AIExamService aiExamService;

    @Override
//    @CacheEvict(value = "set_exams", key = "'set' + #setId")
    public ExamResponseDto createExam(Long setId, CreateExamRequestDto createExamRequestDto) {
        Long userId = authenticationContext.getCurrentUserId();

        Set set = setRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("set not found"));

        if (examRepository.existsByTitleAndSet(createExamRequestDto.title(), set)) {
            throw new ResourceAlreadyExistsException("Exam has been existed");
        }

        Exam exam = examMapper.toExam(createExamRequestDto);
        exam.setCreatedBy(userId);
        exam.setSet(set);

        return examMapper.toExamResponseDto(examRepository.save(exam));
    }

    @Override
//    @Cacheable(value = "set_exams", key = "'set' + #setId")
    public Page<ExamResponseDto> getExam(Long setId, String q, Privacy privacy, Pageable pageable) {
        Long userId = authenticationContext.getCurrentUserId();

        Page<Exam> pagedExam;

        if (q == null || q.isBlank()) {
            if (privacy == null) {
                pagedExam = examRepository.findByCreatedByAndSetId(userId, setId, pageable);
            } else {
                pagedExam = examRepository.findByCreatedByAndSetIdAndPrivacy(userId, setId, privacy.name(), pageable);
            }
        } else {
            if (privacy == null) {
                pagedExam = examRepository.searchByCreatedByAndSetId(userId, setId, q, pageable);
            } else {
                pagedExam = examRepository.searchByCreatedByAndSetIdAndPrivacy(userId, setId, q, privacy.name(), pageable);
            }
        }

        return pagedExam.map(examMapper::toExamResponseDto);
    }

    @Override
//    @Cacheable(value = "exam", key = "#examId")
    public ExamResponseDto getExam(Long setId, Long examId) {
        Exam exam = examRepository.findBySetIdAndId(setId, examId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find exam with id: " + examId));

        return examMapper.toExamResponseDto(exam);
    }

    @Override
    public ExamResponseDto updateExam(Long setId, Long examId, UpdateExamRequestDto updateExamRequestDto) {
        if (!examRepository.existsBySetIdAndId(setId, examId)) {
            throw new ResourceNotFoundException("Cannot find exam with id: " + examId + " in set with id: " + setId);
        }

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find exam with id: " + examId));

        examMapper.updateExamFromDto(updateExamRequestDto, exam);

        return examMapper.toExamResponseDto(examRepository.save(exam));
    }

    @Override
    public void deleteExam(Long setId, Long examId) {
        Exam exam = examRepository.findBySetIdAndId(setId, examId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find exam with id: " + examId));

        examRepository.delete(exam);
    }

    @Override
    public GenerateExamByAIResponseDto generateExamByNotes(GenerateExamByNoteRequestDto request) {
        List<Long> noteIds = request.getNoteIds();
        List<Note> notes = noteRepository.findAllById(noteIds);

        if (notes.isEmpty()) {
            throw new RuntimeException("No notes found with provided IDs");
        }

        List<String> contents = new ArrayList<>();
        for (Note note : notes) {
            String content = "=== Note: " + note.getTitle() + " ===\n" + note.getContent();
            contents.add(content);
        }

        return aiExamService.generateExamByNotes(
                contents,
                request.getQuestions(),
                request.getFreeText(),
                request.getLanguage()
        );
    }
}

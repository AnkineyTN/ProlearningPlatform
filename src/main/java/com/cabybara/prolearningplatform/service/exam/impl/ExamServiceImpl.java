package com.cabybara.prolearningplatform.service.exam.impl;

import com.cabybara.prolearningplatform.dto.request.exam.CreateExamRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.GenerateExamByFileRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.GenerateExamByNoteRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.UpdateExamRequestDto;
import com.cabybara.prolearningplatform.dto.response.exam.ExamResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.GenerateExamByAIResponseDto;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    public List<ExamResponseDto> getExam(Long setId, Pageable pageable) {
        Set set = setRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("set not found"));

        return examRepository.findAllBySet(set, pageable).stream()
                .map(examMapper::toExamResponseDto)
                .toList();
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
    public GenerateExamByAIResponseDto generateExamByFiles(GenerateExamByFileRequestDto request) {
        List<MultipartFile> files = request.getFiles();

        StringBuilder allContent = new StringBuilder();

        for (MultipartFile file : files) {
            try {
                String fileName = file.getOriginalFilename();

                // Log file information
                System.out.println("Processing file: " + fileName + " - Size: " + file.getSize());

                // Read file
                String content = fileService.readFile(file);

                // Add separator between two files
                allContent.append("=== Content from: ").append(fileName).append(" ===\n");
                allContent.append(content);
                allContent.append("\n\n");

            } catch (Exception e) {
                throw new RuntimeException("Error processing file: " + file.getOriginalFilename() + " - " + e.getMessage());
            }
        }

        // Build request body for AI API Call
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("content", allContent.toString());
        requestBody.put("questions", request.getQuestions());
        requestBody.put("language", request.getLanguage());
        requestBody.put("type", "file");

        return aiExamService.generateExam(requestBody);
    }

    @Override
    public GenerateExamByAIResponseDto generateExamByNotes(GenerateExamByNoteRequestDto request) {
        List<Long> noteIds = request.getNoteIds();

        List<Note> notes = noteRepository.findAllById(noteIds);

        if (notes.isEmpty()) {
            throw new RuntimeException("No notes found with provided IDs");
        }

        StringBuilder allContent = new StringBuilder();
        for (Note note : notes) {
            allContent.append("=== Note: ").append(note.getTitle()).append(" ===\n");
            allContent.append(note.getContent()).append("\n\n");
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("content", allContent.toString());
        requestBody.put("questions", request.getQuestions());
        requestBody.put("language", request.getLanguage());
        requestBody.put("type", "note");

        return aiExamService.generateExam(requestBody);
    }
}

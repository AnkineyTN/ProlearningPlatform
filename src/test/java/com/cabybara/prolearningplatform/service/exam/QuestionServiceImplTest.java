package com.cabybara.prolearningplatform.service.exam;

import com.cabybara.prolearningplatform.dto.request.exam.CreateQuestionRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.QuestionOptionDto;
import com.cabybara.prolearningplatform.dto.request.exam.UpdateQuestionRequestDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuestionListResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuestionResponseDto;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.mapper.ExamMapper;
import com.cabybara.prolearningplatform.model.exam.Exam;
import com.cabybara.prolearningplatform.model.exam.ExamQuestion;
import com.cabybara.prolearningplatform.model.exam.Question;
import com.cabybara.prolearningplatform.model.exam.QuestionOption;
import com.cabybara.prolearningplatform.repository.ExamQuestionRepository;
import com.cabybara.prolearningplatform.repository.ExamRepository;
import com.cabybara.prolearningplatform.repository.QuestionRepository;
import com.cabybara.prolearningplatform.service.exam.impl.QuestionServiceImpl;
import com.cabybara.prolearningplatform.service.permission.impl.ExamPermissionService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {

    @Mock
    private ExamRepository examRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private ExamQuestionRepository examQuestionRepository;
    @Mock
    private ExamMapper examMapper;
    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private ExamPermissionService examPermissionService;

    @Test
    void createQuestionsWithOptions() {
        QuestionServiceImpl service = new QuestionServiceImpl(
                examRepository, questionRepository, examQuestionRepository, examMapper, authenticationContext, examPermissionService);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(examPermissionService.canEdit(1L, 1L)).thenReturn(true);

        Exam exam = new Exam();
        exam.setId(1L);
        when(examRepository.findById(1L)).thenReturn(Optional.of(exam));
        when(examQuestionRepository.countByExamId(1L)).thenReturn(0);

        QuestionOptionDto optionDto1 = QuestionOptionDto.builder()
                .optionText("3").isCorrect(false).build();
        QuestionOptionDto optionDto2 = QuestionOptionDto.builder()
                .optionText("4").isCorrect(true).build();
        QuestionOptionDto optionDto3 = QuestionOptionDto.builder()
                .optionText("5").isCorrect(false).build();
        QuestionOptionDto optionDto4 = QuestionOptionDto.builder()
                .optionText("6").isCorrect(false).build();

        CreateQuestionRequestDto dto = CreateQuestionRequestDto.builder()
                .content("What is 2+2?")
                .type("MULTIPLE_CHOICE")
                .point(1)
                .options(List.of(optionDto1, optionDto2, optionDto3, optionDto4))
                .build();

        Question question = new Question();
        question.setId(10L);
        question.setContent("What is 2+2?");
        question.setOptions(new ArrayList<>());
        when(examMapper.toQuestion(dto)).thenReturn(question);

        List<QuestionResponseDto> questionResponseDtos = List.of(
                QuestionResponseDto.builder().id(10L).content("What is 2+2?").build()
        );
        when(examMapper.toQuestionResponseDto(any(ExamQuestion.class)))
                .thenReturn(questionResponseDtos.get(0));

        QuestionListResponseDto response = service.createQuestion(1L, List.of(dto));

        verify(questionRepository).saveAll(anyList());
        verify(examQuestionRepository).saveAll(anyList());
        assertEquals(1, response.questions().size());
    }

    @Test
    void createQuestionWithoutCorrectOptionSavesAnyway() {
        QuestionServiceImpl service = new QuestionServiceImpl(
                examRepository, questionRepository, examQuestionRepository, examMapper, authenticationContext, examPermissionService);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(examPermissionService.canEdit(1L, 1L)).thenReturn(true);

        Exam exam = new Exam();
        exam.setId(1L);
        when(examRepository.findById(1L)).thenReturn(Optional.of(exam));
        when(examQuestionRepository.countByExamId(1L)).thenReturn(0);

        QuestionOptionDto optionDto = QuestionOptionDto.builder()
                .optionText("Answer").isCorrect(false).build();

        CreateQuestionRequestDto dto = CreateQuestionRequestDto.builder()
                .content("Explain")
                .type("ESSAY")
                .point(5)
                .options(List.of(optionDto))
                .build();

        Question question = new Question();
        question.setId(11L);
        when(examMapper.toQuestion(dto)).thenReturn(question);

        QuestionResponseDto respDto = QuestionResponseDto.builder().id(11L).content("Explain").build();
        when(examMapper.toQuestionResponseDto(any(ExamQuestion.class))).thenReturn(respDto);

        service.createQuestion(1L, List.of(dto));

        verify(questionRepository).saveAll(anyList());
    }

    @Test
    void updateQuestionChangesOptions() {
        QuestionServiceImpl service = new QuestionServiceImpl(
                examRepository, questionRepository, examQuestionRepository, examMapper, authenticationContext, examPermissionService);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(examPermissionService.canEdit(1L, 1L)).thenReturn(true);
        when(examRepository.existsById(1L)).thenReturn(true);

        Question existing = new Question();
        existing.setId(1L);
        existing.setContent("Old content");
        existing.setOptions(new ArrayList<>());

        when(examQuestionRepository.findByExamIdAndQuestionId(1L, 1L))
                .thenReturn(Optional.of(new ExamQuestion()));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(questionRepository.save(existing)).thenReturn(existing);

        QuestionResponseDto respDto = QuestionResponseDto.builder().id(1L).content("New content").build();
        when(examMapper.toQuestionResponseDto(existing)).thenReturn(respDto);

        QuestionOptionDto newOption = QuestionOptionDto.builder()
                .optionText("New answer").isCorrect(true).build();

        UpdateQuestionRequestDto dto = UpdateQuestionRequestDto.builder()
                .content("New content")
                .options(List.of(newOption))
                .build();

        QuestionOption mappedOption = new QuestionOption();
        mappedOption.setOptionText("New answer");
        mappedOption.setIsCorrect(true);
        when(examMapper.toQuestionOptionList(dto.options())).thenReturn(List.of(mappedOption));

        QuestionResponseDto response = service.updateQuestion(1L, 1L, dto);

        assertEquals("New content", response.content());
        verify(questionRepository).save(existing);
    }

    @Test
    void deleteQuestionRemovesFromExam() {
        QuestionServiceImpl service = new QuestionServiceImpl(
                examRepository, questionRepository, examQuestionRepository, examMapper, authenticationContext, examPermissionService);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(examPermissionService.canEdit(1L, 1L)).thenReturn(true);
        when(examRepository.existsById(1L)).thenReturn(true);
        when(examQuestionRepository.findByExamIdAndQuestionId(1L, 1L))
                .thenReturn(Optional.of(new ExamQuestion()));

        service.deleteQuestion(1L, 1L);

        verify(examQuestionRepository).deleteByExamIdAndQuestionId(1L, 1L);
        verify(questionRepository).deleteById(1L);
    }

    @Test
    void deleteQuestionFromNonExistentExamThrows() {
        QuestionServiceImpl service = new QuestionServiceImpl(
                examRepository, questionRepository, examQuestionRepository, examMapper, authenticationContext, examPermissionService);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(examPermissionService.canEdit(1L, 99L)).thenReturn(true);
        when(examRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteQuestion(99L, 1L));
    }
}

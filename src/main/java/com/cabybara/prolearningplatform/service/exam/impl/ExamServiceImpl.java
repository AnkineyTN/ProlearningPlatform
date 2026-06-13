package com.cabybara.prolearningplatform.service.exam.impl;

import com.cabybara.prolearningplatform.dto.internal.CardContent;
import com.cabybara.prolearningplatform.dto.internal.QuestionContent;
import com.cabybara.prolearningplatform.dto.request.exam.CreateExamFromReviewRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.CreateExamRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.GenerateExamByNoteRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.NoteContentDto;
import com.cabybara.prolearningplatform.dto.request.exam.NoteRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.AIGenerateExamByNoteRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.UpdateExamRequestDto;
import com.cabybara.prolearningplatform.dto.request.share.InviteMemberRequest;
import com.cabybara.prolearningplatform.dto.response.exam.ExamResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.SharedExamResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.GenerateExamByAIResponseDto;
import com.cabybara.prolearningplatform.dto.response.note.AcceptByTokenResponse;
import com.cabybara.prolearningplatform.dto.response.share.InviteResultResponse;
import com.cabybara.prolearningplatform.dto.response.share.PendingInviteResponse;
import com.cabybara.prolearningplatform.enums.CreationMethod;
import com.cabybara.prolearningplatform.enums.NoteRole;
import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.exception.BadRequestException;
import com.cabybara.prolearningplatform.exception.ResourceAlreadyExistsException;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.mapper.ExamMapper;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.exam.Exam;
import com.cabybara.prolearningplatform.model.exam.ExamQuestion;
import com.cabybara.prolearningplatform.model.exam.Question;
import com.cabybara.prolearningplatform.model.note.Note;
import com.cabybara.prolearningplatform.repository.CardItemRepository;
import com.cabybara.prolearningplatform.repository.ExamQuestionRepository;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.repository.ExamRepository;
import com.cabybara.prolearningplatform.repository.SetRepository;
import com.cabybara.prolearningplatform.service.ai.AIExamService;
import com.cabybara.prolearningplatform.service.exam.ExamService;
import com.cabybara.prolearningplatform.service.exam.QuestionService;
import com.cabybara.prolearningplatform.service.file.FileService;
import com.cabybara.prolearningplatform.service.knowledge.TopicAssignmentAsyncService;
import com.cabybara.prolearningplatform.service.permission.impl.ExamPermissionService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final FileService fileService;
    private final AIExamService aiExamService;
    private final QuestionService questionService;

    private final AuthenticationContext authenticationContext;
    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final FlashcardRepository flashcardRepository;
    private final CardItemRepository cardItemRepository;
    private final SetRepository setRepository;
    private final NoteRepository noteRepository;
    private final ExamMapper examMapper;
    private final ExamPermissionService examPermissionService;
    private final TopicAssignmentAsyncService topicAssignmentAsyncService;
    private final com.cabybara.prolearningplatform.repository.UserFavoriteResourceRepository userFavoriteResourceRepository;
    private final com.cabybara.prolearningplatform.repository.UserRepository userRepository;

    @Override
    @Transactional
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

        Exam savedExam = examRepository.save(exam);
        examPermissionService.addOwner(savedExam.getId(), userId);
        setRepository.updateLastModifiedDate(setId, OffsetDateTime.now());
        return examMapper.toExamResponseDto(savedExam);
    }

    @Override
    @Transactional
    public ExamResponseDto createExamFromReview(CreateExamFromReviewRequestDto dto, Long setId) {
        return persistExam(dto, setId, CreationMethod.REVIEW);
    }

    @Override
    @Transactional
    public ExamResponseDto createExamFromFlashcard(Long setId, Long flashcardId) {
        flashcardRepository.findByIdAndSetId(flashcardId, setId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found with id: " + flashcardId));

        List<CardContent> cards = cardItemRepository.findAllByFlashcardId(flashcardId)
                .stream()
                .map(card -> new CardContent(card.getFrontCard(), card.getBackCard()))
                .toList();

        if (cards.isEmpty()) {
            throw new BadRequestException("Flashcard has no cards to generate exam from");
        }

        return persistExam(aiExamService.generateExamFromCards(cards), setId, CreationMethod.AI);
    }

    private ExamResponseDto persistExam(CreateExamFromReviewRequestDto content, Long setId, CreationMethod method) {
        Long userId = authenticationContext.getCurrentUserId();

        Exam exam = new Exam();
        exam.setTitle(content.title());
        exam.setDescription(content.description());
        exam.setDuration(content.duration());
        exam.setCreatedBy(userId);
        exam.setSet(setId != null ? setRepository.getReferenceById(setId) : null);
        exam.setPrivacy(Privacy.PRIVATE);
        exam.setCreationMethod(method);

        Exam savedExam = examRepository.save(exam);
        Long examId = savedExam.getId();
        examPermissionService.addOwner(examId, userId);

        if (content.questions() != null && !content.questions().isEmpty()) {
            questionService.createQuestion(examId, content.questions());
        }

        if (method == CreationMethod.AI) {
            topicAssignmentAsyncService.assignTopicsToExamAsync(examId);
        }

        if (setId != null) {
            setRepository.updateLastModifiedDate(setId, OffsetDateTime.now());
        }
        return examMapper.toExamResponseDto(examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found after save")));
    }

    @Override
    public Page<ExamResponseDto> getExam(Long setId, String q, Privacy privacy, CreationMethod createMethod, Pageable pageable) {
        Long userId = authenticationContext.getCurrentUserId();

        Page<Exam> pagedExam;

        String methodFilter = createMethod != null ? createMethod.name() : null;

        if (q == null || q.isBlank()) {
            if (privacy == null) {
                pagedExam = examRepository.findByCreatedByAndSetId(userId, setId, methodFilter, pageable);
            } else {
                pagedExam = examRepository.findByCreatedByAndSetIdAndPrivacy(userId, setId, privacy.name(), methodFilter, pageable);
            }
        } else {
            if (privacy == null) {
                pagedExam = examRepository.searchByCreatedByAndSetId(userId, setId, q, methodFilter, pageable);
            } else {
                pagedExam = examRepository.searchByCreatedByAndSetIdAndPrivacy(userId, setId, q, privacy.name(), methodFilter, pageable);
            }
        }

        return pagedExam.map(exam -> {
            ExamResponseDto dto = examMapper.toExamResponseDto(exam);
            boolean isFavorited = userFavoriteResourceRepository.existsByUserIdAndResourceIdAndResourceType(userId, exam.getId(), com.cabybara.prolearningplatform.enums.ContentType.EXAM);
            
            com.cabybara.prolearningplatform.model.User owner = exam.getCreatedBy() != null ? userRepository.findById(exam.getCreatedBy()).orElse(null) : null;
            String ownerName = owner != null ? (owner.getFirstName() != null ? owner.getFirstName() + " " + owner.getLastName() : owner.getLastName()) : null;
            String ownerAvatar = owner != null ? owner.getAvatarUrl() : null;

            return new ExamResponseDto(
                dto.id(), dto.title(), dto.privacy(), dto.description(), dto.duration(), dto.numQuestions(), dto.creationMethod(), dto.createdAt(), dto.updatedAt(), dto.userRole(), isFavorited,
                exam.getCreatedBy(), ownerName, ownerAvatar
            );
        });
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public ExamResponseDto createReviewExamFromQuestions(Long examId, List<Long> questionIds) {
        Exam sourceExam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));

        Long setId = sourceExam.getSet() != null ? sourceExam.getSet().getId() : null;

        Map<Long, ExamQuestion> questionMap = examQuestionRepository.findAllByExamId(examId)
                .stream()
                .collect(Collectors.toMap(eq -> eq.getQuestion().getId(), eq -> eq));

        for (Long qId : questionIds) {
            if (!questionMap.containsKey(qId)) {
                throw new BadRequestException("Question " + qId + " does not belong to exam " + examId);
            }
        }

        List<QuestionContent> contents = questionIds.stream()
                .map(questionMap::get)
                .map(eq -> {
                    Question q = eq.getQuestion();
                    List<String> options = q.getOptions().stream()
                            .map(opt -> opt.getOptionText())
                            .toList();
                    String correctAnswer = q.getOptions().stream()
                            .filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect()))
                            .map(opt -> opt.getOptionText())
                            .findFirst()
                            .orElse(q.getExpectedAnswer());
                    return new QuestionContent(q.getContent(), options, correctAnswer);
                })
                .toList();

        CreateExamFromReviewRequestDto dto = aiExamService.generateExamFromQuestions(contents);
        return createExamFromReview(dto, setId);
    }

    @Override
    @Cacheable(value = "exam_detail", key = "@authenticationContext.getCurrentUserId() + ':' + #setId + ':' + #examId")
    public ExamResponseDto getExam(Long setId, Long examId) {
        Long userId = authenticationContext.getCurrentUserId();
        
        Exam exam = examRepository.findBySetIdAndId(setId, examId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find exam with id: " + examId));

        NoteRole userRole = examPermissionService.getUserRoleInExam(examId, userId);
        boolean isFavorited = userFavoriteResourceRepository.existsByUserIdAndResourceIdAndResourceType(userId, examId, com.cabybara.prolearningplatform.enums.ContentType.EXAM);

        com.cabybara.prolearningplatform.model.User owner = exam.getCreatedBy() != null ? userRepository.findById(exam.getCreatedBy()).orElse(null) : null;
        String ownerName = owner != null ? (owner.getFirstName() != null ? owner.getFirstName() + " " + owner.getLastName() : owner.getLastName()) : null;
        String ownerAvatar = owner != null ? owner.getAvatarUrl() : null;

        ExamResponseDto dto = examMapper.toExamResponseDto(exam);
        return new ExamResponseDto(
            dto.id(),
            dto.title(),
            dto.privacy(),
            dto.description(),
            dto.duration(),
            dto.numQuestions(),
            dto.creationMethod(),
            dto.createdAt(),
            dto.updatedAt(),
            userRole,
            isFavorited,
            exam.getCreatedBy(),
            ownerName,
            ownerAvatar
        );
    }

    @Override
    @Transactional
    @CacheEvict(value = "exam_detail", allEntries = true)
    public ExamResponseDto updateExam(Long setId, Long examId, UpdateExamRequestDto updateExamRequestDto) {
        if (!examRepository.existsBySetIdAndId(setId, examId)) {
            throw new ResourceNotFoundException("Cannot find exam with id: " + examId + " in set with id: " + setId);
        }

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find exam with id: " + examId));

        examMapper.updateExamFromDto(updateExamRequestDto, exam);

        ExamResponseDto result = examMapper.toExamResponseDto(examRepository.save(exam));
        setRepository.updateLastModifiedDate(setId, OffsetDateTime.now());
        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = "exam_detail", allEntries = true)
    public void deleteExam(Long setId, Long examId) {
        Exam exam = examRepository.findBySetIdAndId(setId, examId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find exam with id: " + examId));

        examRepository.delete(exam);
        setRepository.updateLastModifiedDate(setId, OffsetDateTime.now());
    }

    @Override
    public GenerateExamByAIResponseDto generateExamByNotes(GenerateExamByNoteRequestDto request) {
        // Get note IDs from the request
        List<Long> noteIds = request.getNotes().stream()
                .map(NoteRequestDto::getNoteId)
                .collect(Collectors.toList());
        
        List<Note> notes = noteRepository.findAllById(noteIds);

        if (notes.isEmpty()) {
            throw new RuntimeException("No notes found with provided IDs");
        }

        // Build a map of noteId to document URLs for quick lookup
        Map<Long, List<String>> noteIdToUrlsMap = request.getNotes().stream()
                .collect(Collectors.toMap(
                        NoteRequestDto::getNoteId,
                        NoteRequestDto::getDocumentUrls
                ));

        // Build NoteContentDto list with content and URLs
        List<NoteContentDto> noteContents = new ArrayList<>();
        for (Note note : notes) {
            List<String> documentUrls = noteIdToUrlsMap.getOrDefault(note.getId(), new ArrayList<>());
            
            NoteContentDto noteContent = NoteContentDto.builder()
                    .noteId(note.getId())
                    .content(note.getContent())
                    .documentUrls(documentUrls)
                    .build();

            noteContents.add(noteContent);
        }

        // Build the AI request DTO
        AIGenerateExamByNoteRequestDto aiRequest = AIGenerateExamByNoteRequestDto.builder()
                .notes(noteContents)
                .questions(request.getQuestions())
                .difficulty(request.getDifficulty())
                .freeText(request.getFreeText())
                .language(request.getLanguage())
                .build();

        return aiExamService.generateExamByNotes(aiRequest);
    }

    @Override
    public List<InviteResultResponse> inviteMembers(Long setId, Long examId, InviteMemberRequest request) {
        Long userId = authenticationContext.getCurrentUserId();
        List<InviteResultResponse> results = examPermissionService.inviteMembers(
            setId, examId, request.getTargets(), request.getRole(), userId);

        return results;
    }

    @Override
    @CacheEvict(value = "exam_detail", allEntries = true)
    public void acceptInvite(Long examId) {
        examPermissionService.acceptInvite(examId, authenticationContext.getCurrentUserId());
    }

    @Override
    @CacheEvict(value = "exam_detail", allEntries = true)
    public void declineInvite(Long examId) {
        examPermissionService.declineInvite(examId, authenticationContext.getCurrentUserId());
    }

    @Override
    @CacheEvict(value = "exam_detail", allEntries = true)
    public void removeMember(Long examId, Long targetUserId) {
        examPermissionService.removeMember(examId, targetUserId, authenticationContext.getCurrentUserId());
    }

    @Override
    public AcceptByTokenResponse acceptByToken(String token) {
        AcceptByTokenResponse response = examPermissionService.acceptByToken(token);

        return response;
    }

    @Override
    public List<PendingInviteResponse> getPendingInvites() {
        Long userId = authenticationContext.getCurrentUserId();
        List<InviteResultResponse.PendingInviteExamResponse> examResponses = 
            examPermissionService.getPendingInvites(userId);
        
        return examResponses.stream()
                .map(exam -> new PendingInviteResponse(
                    exam.getExamId(),
                    exam.getExamTitle(),
                    exam.getRole(),
                    exam.getSetId(),
                    exam.getInvitedAt()
                ))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Page<SharedExamResponseDto> getSharedExams(String q, Privacy privacy, CreationMethod createMethod, Pageable pageable) {
        Long userId = authenticationContext.getCurrentUserId();
        String privacyFilter = privacy != null ? privacy.name() : null;
        String methodFilter = createMethod != null ? createMethod.name() : null;

        return examRepository.findSharedExams(userId, q, privacyFilter, methodFilter, pageable)
                .map(exam -> {
                    NoteRole role = examPermissionService.getUserRoleInExam(exam.getId(), userId);
                    ExamResponseDto dto = examMapper.toExamResponseDto(exam);
                    return new SharedExamResponseDto(
                            dto.id(),
                            dto.title(),
                            dto.privacy(),
                            dto.description(),
                            dto.duration(),
                            dto.numQuestions(),
                            dto.creationMethod(),
                            dto.createdAt(),
                            dto.updatedAt(),
                            role,
                            exam.getSet() != null ? exam.getSet().getId() : null,
                            userFavoriteResourceRepository.existsByUserIdAndResourceIdAndResourceType(userId, exam.getId(), com.cabybara.prolearningplatform.enums.ContentType.EXAM)
                    );
                });
    }
}

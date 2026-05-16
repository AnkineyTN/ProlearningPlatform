package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.response.admin.AdminPlatformStatsResponseDto;
import com.cabybara.prolearningplatform.repository.ExamRepository;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.repository.FlashcardStudySessionRepository;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.repository.PomodoroSessionRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
@Tag(name = "Admin — Platform Stats", description = "Aggregate platform statistics for administrators")
@SecurityRequirement(name = "bearerAuth")
public class AdminPlatformStatsController {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final FlashcardRepository flashcardRepository;
    private final ExamRepository examRepository;
    private final PomodoroSessionRepository pomodoroSessionRepository;
    private final FlashcardStudySessionRepository flashcardStudySessionRepository;

    @Operation(summary = "Platform statistics", description = "Returns aggregate counts: users, notes, flashcards, exams, sessions.")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<AdminPlatformStatsResponseDto>> getStats() {
        long totalUsers = userRepository.count();
        long blockedUsers = userRepository.findByIsBlocked(true, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
        long proUsers = userRepository.aggregateAccountType().stream()
                .filter(r -> "PRO".equals(String.valueOf(r[0])))
                .mapToLong(r -> ((Number) r[1]).longValue())
                .sum();
        long freeUsers = userRepository.aggregateAccountType().stream()
                .filter(r -> "FREE".equals(String.valueOf(r[0])))
                .mapToLong(r -> ((Number) r[1]).longValue())
                .sum();

        AdminPlatformStatsResponseDto stats = AdminPlatformStatsResponseDto.builder()
                .totalUsers(totalUsers)
                .blockedUsers(blockedUsers)
                .proUsers(proUsers)
                .freeUsers(freeUsers)
                .totalNotes(noteRepository.count())
                .totalFlashcards(flashcardRepository.count())
                .totalExams(examRepository.count())
                .totalPomodoroSessions(pomodoroSessionRepository.count())
                .totalStudySessions(flashcardStudySessionRepository.count())
                .build();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", stats, null));
    }
}

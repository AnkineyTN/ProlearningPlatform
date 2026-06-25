package com.cabybara.prolearningplatform.service.admin.impl;

import com.cabybara.prolearningplatform.dto.response.admin.AdminPlatformStatsResponseDto;
import com.cabybara.prolearningplatform.repository.ExamRepository;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.repository.FlashcardStudySessionRepository;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.repository.PomodoroSessionRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.admin.AdminPlatformStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminPlatformStatsServiceImpl implements AdminPlatformStatsService {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final FlashcardRepository flashcardRepository;
    private final ExamRepository examRepository;
    private final PomodoroSessionRepository pomodoroSessionRepository;
    private final FlashcardStudySessionRepository flashcardStudySessionRepository;

    @Override
    public AdminPlatformStatsResponseDto getStats() {
        long totalUsers = userRepository.count();
        long blockedUsers = userRepository.findByIsBlocked(true, Pageable.unpaged()).getTotalElements();

        long proUsers = 0;
        long freeUsers = 0;
        for (var r : userRepository.aggregateAccountType()) {
            if ("PRO".equals(r.getLabel())) {
                proUsers = r.getCount();
            } else {
                freeUsers += r.getCount();
            }
        }

        return AdminPlatformStatsResponseDto.builder()
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
    }
}

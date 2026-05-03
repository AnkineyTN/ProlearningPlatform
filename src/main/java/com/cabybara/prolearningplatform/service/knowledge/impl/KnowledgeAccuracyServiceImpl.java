package com.cabybara.prolearningplatform.service.knowledge.impl;

import com.cabybara.prolearningplatform.dto.helper.ExamAnswerTopicStat;
import com.cabybara.prolearningplatform.dto.response.knowledge.TopicAccuracyDto;
import com.cabybara.prolearningplatform.model.flashcard_study_session.StudySessionReviewLog;
import com.cabybara.prolearningplatform.repository.ExamAttemptRepository;
import com.cabybara.prolearningplatform.repository.StudySessionReviewLogRepository;
import com.cabybara.prolearningplatform.service.knowledge.KnowledgeAccuracyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class KnowledgeAccuracyServiceImpl implements KnowledgeAccuracyService {

    private final StudySessionReviewLogRepository reviewLogRepository;
    private final ExamAttemptRepository examAttemptRepository;

    @Override
    public List<TopicAccuracyDto> computeFromSession(Long sessionId) {
        List<StudySessionReviewLog> logs = reviewLogRepository.findBySessionIdOrderByReviewedAtAsc(sessionId);

        // last-review-wins: logs are ordered ASC, so later entries overwrite earlier ones
        Map<Long, StudySessionReviewLog> lastByCard = new LinkedHashMap<>();
        for (StudySessionReviewLog log : logs) {
            lastByCard.put(log.getCard().getId(), log);
        }

        // group by topic
        Map<String, List<Boolean>> byTopic = new LinkedHashMap<>();
        for (StudySessionReviewLog log : lastByCard.values()) {
            String topic = log.getCard().getTopic();
            if (topic != null) {
                byTopic.computeIfAbsent(topic, k -> new ArrayList<>()).add(log.isKnown());
            }
        }

        return buildAccuracies(byTopic);
    }

    @Override
    public List<TopicAccuracyDto> computeFromAttempt(Long attemptId, Long examId) {
        List<ExamAnswerTopicStat> stats = examAttemptRepository.findAnswerTopicStatsByAttempt(attemptId, examId);

        Map<String, List<Double>> scoresByTopic = new LinkedHashMap<>();

        for (ExamAnswerTopicStat stat : stats) {
            String topic = stat.getTopic();
            if (topic == null) continue;

            double earnedScore = resolveScore(stat);
            if (earnedScore < 0) continue; // skip (essay not graded yet)

            scoresByTopic.computeIfAbsent(topic, k -> new ArrayList<>()).add(earnedScore);
        }

        List<TopicAccuracyDto> result = new ArrayList<>();
        for (Map.Entry<String, List<Double>> entry : scoresByTopic.entrySet()) {
            double accuracy = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0);
            result.add(new TopicAccuracyDto(entry.getKey(), accuracy));
        }
        return result;
    }

    private double resolveScore(ExamAnswerTopicStat stat) {
        if (stat.getIsCorrect() != null) {
            // MULTIPLE_CHOICE or TRUE_FALSE
            return stat.getIsCorrect() ? 1.0 : 0.0;
        }
        // ESSAY
        if (stat.getEarnedPoints() != null && stat.getPoints() != null && stat.getPoints() > 0) {
            return stat.getEarnedPoints() / stat.getPoints();
        }
        return -1; // not graded yet — skip
    }

    private List<TopicAccuracyDto> buildAccuracies(Map<String, List<Boolean>> byTopic) {
        List<TopicAccuracyDto> result = new ArrayList<>();
        for (Map.Entry<String, List<Boolean>> entry : byTopic.entrySet()) {
            long correct = entry.getValue().stream().filter(Boolean::booleanValue).count();
            double accuracy = (double) correct / entry.getValue().size();
            result.add(new TopicAccuracyDto(entry.getKey(), accuracy));
        }
        return result;
    }
}

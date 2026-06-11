package com.cabybara.prolearningplatform.service.social.impl;

import com.cabybara.prolearningplatform.dto.response.social.*;
import com.cabybara.prolearningplatform.enums.ContentType;
import com.cabybara.prolearningplatform.enums.ResourceType;
import com.cabybara.prolearningplatform.enums.TrendingPeriod;
import com.cabybara.prolearningplatform.repository.*;
import com.cabybara.prolearningplatform.service.social.SocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SocialServiceImpl implements SocialService {

    private final NoteRepository noteRepository;
    private final FlashcardRepository flashcardRepository;
    private final ExamRepository examRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final ResourceViewLogRepository resourceViewLogRepository;
    private final CardItemRepository cardItemRepository;

    @Override
    public Page<SocialItemResponseDto> getSocialNotes(String q, Pageable pageable) {
        return noteRepository.findSocialNotes(q, pageable)
                .map(sn -> new SocialItemResponseDto(
                        sn.getId(),
                        sn.getSetId(),
                        "NOTE",
                        sn.getTitle(),
                        sn.getDescription(),
                        toOffsetDateTime(sn.getCreatedAt()),
                        toOffsetDateTime(sn.getUpdatedAt()),
                        sn.getOwnerId(),
                        buildOwnerName(sn.getOwnerFirstName(), sn.getOwnerLastName()),
                        sn.getOwnerAvatarUrl(),
                        null,
                        null,
                        null
                ));
    }

    @Override
    public Page<SocialItemResponseDto> getSocialFlashcards(String q, Pageable pageable) {
        return flashcardRepository.findSocialFlashcards(q, pageable)
                .map(sf -> new SocialItemResponseDto(
                        sf.getId(),
                        sf.getSetId(),
                        "FLASHCARD",
                        sf.getTitle(),
                        sf.getDescription(),
                        toOffsetDateTime(sf.getCreatedAt()),
                        toOffsetDateTime(sf.getUpdatedAt()),
                        sf.getOwnerId(),
                        buildOwnerName(sf.getOwnerFirstName(), sf.getOwnerLastName()),
                        sf.getOwnerAvatarUrl(),
                        sf.getNumCards(),
                        null,
                        null
                ));
    }

    @Override
    public Page<SocialItemResponseDto> getSocialExams(String q, Pageable pageable) {
        return examRepository.findSocialExams(q, pageable)
                .map(se -> new SocialItemResponseDto(
                        se.getId(),
                        se.getSetId(),
                        "EXAM",
                        se.getTitle(),
                        se.getDescription(),
                        toOffsetDateTime(se.getCreatedAt()),
                        toOffsetDateTime(se.getUpdatedAt()),
                        se.getOwnerId(),
                        buildOwnerName(se.getOwnerFirstName(), se.getOwnerLastName()),
                        se.getOwnerAvatarUrl(),
                        null,
                        se.getNumQuestions(),
                        se.getDuration()
                ));
    }

    @Override
    public List<TrendingResourceResponseDto> getTrendingResources(TrendingPeriod period, int topN, ResourceType type) {
        LocalDate startLocalDate = period.toSinceLocalDate();
        OffsetDateTime startDateTime = period.toSinceDateTime();

        Map<String, Long> viewCounts = new HashMap<>();
        Map<String, Long> sessionCounts = new HashMap<>();
        Map<String, ContentType> resourceTypes = new HashMap<>();
        Map<String, Long> resourceIds = new HashMap<>();

        // Process views
        ContentType filterType = (type != null) ? type.toContentType() : null;
        List<Object[]> viewsList;
        if (filterType == null) {
            viewsList = resourceViewLogRepository.findTopResourcesByViews(startDateTime, PageRequest.of(0, topN * 10));
        } else {
            viewsList = resourceViewLogRepository.findTopResourcesByViewsAndType(filterType, startDateTime, PageRequest.of(0, topN * 10));
        }
        for (Object[] row : viewsList) {
            Long rId = ((Number) row[0]).longValue();
            ContentType rType = ContentType.valueOf(String.valueOf(row[1]));
            Long count = ((Number) row[2]).longValue();
            String key = rType.name() + "_" + rId;
            viewCounts.put(key, count);
            resourceTypes.put(key, rType);
            resourceIds.put(key, rId);
        }

        // Process sessions
        List<String> allowedTypesStr = List.of(ContentType.NOTE.name(), ContentType.FLASHCARD.name(), ContentType.EXAM.name());
        List<Object[]> sessionsList = activityLogRepository.findTopResourcesBySessions(
                startLocalDate, allowedTypesStr, PageRequest.of(0, topN * 10));
        for (Object[] row : sessionsList) {
            Long rId = ((Number) row[0]).longValue();
            ContentType rType = ContentType.valueOf(String.valueOf(row[1]));
            Long count = ((Number) row[2]).longValue();

            // Filter by type if not ALL
            if (filterType != null && rType != filterType) {
                continue;
            }

            String key = rType.name() + "_" + rId;
            sessionCounts.put(key, count);
            resourceTypes.put(key, rType);
            resourceIds.put(key, rId);
        }

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(viewCounts.keySet());
        allKeys.addAll(sessionCounts.keySet());

        class ScoredResource {
            Long id;
            ContentType type;
            long score;
            long views;
            long sessions;
        }

        List<ScoredResource> scoredList = new ArrayList<>();
        for (String key : allKeys) {
            long views = viewCounts.getOrDefault(key, 0L);
            long sessions = sessionCounts.getOrDefault(key, 0L);
            long score = views + 3 * sessions;

            ScoredResource sr = new ScoredResource();
            sr.id = resourceIds.get(key);
            sr.type = resourceTypes.get(key);
            sr.score = score;
            sr.views = views;
            sr.sessions = sessions;
            scoredList.add(sr);
        }

        // Sort by score desc
        scoredList.sort((a, b) -> Long.compare(b.score, a.score));

        List<TrendingResourceResponseDto> result = new ArrayList<>();
        int rank = 1;
        for (ScoredResource sr : scoredList) {
            if (result.size() >= topN) {
                break;
            }
            Long rId = sr.id;
            String title = "";
            String description = "";
            Long ownerId = null;
            String ownerName = "";
            Long setId = null;

            if (sr.type == ContentType.NOTE) {
                var optNote = noteRepository.findById(rId);
                if (optNote.isPresent()) {
                    var note = optNote.get();
                    title = note.getTitle();
                    description = note.getDescription();
                    setId = note.getSet() != null ? note.getSet().getId() : null;
                    if (note.getUser() != null) {
                        ownerId = note.getUser().getId();
                        ownerName = buildOwnerName(note.getUser().getFirstName(), note.getUser().getLastName());
                    }
                } else {
                    continue; // Skip if resource deleted
                }
            } else if (sr.type == ContentType.FLASHCARD) {
                var optFc = flashcardRepository.findById(rId);
                if (optFc.isPresent()) {
                    var fc = optFc.get();
                    title = fc.getTitle();
                    description = fc.getDescription();
                    setId = fc.getSet() != null ? fc.getSet().getId() : null;
                    if (fc.getUser() != null) {
                        ownerId = fc.getUser().getId();
                        ownerName = buildOwnerName(fc.getUser().getFirstName(), fc.getUser().getLastName());
                    }
                } else {
                    continue;
                }
            } else if (sr.type == ContentType.EXAM) {
                var optExam = examRepository.findById(rId);
                if (optExam.isPresent()) {
                    var exam = optExam.get();
                    title = exam.getTitle();
                    description = exam.getDescription();
                    setId = exam.getSet() != null ? exam.getSet().getId() : null;
                    ownerId = exam.getCreatedBy();
                    if (ownerId != null) {
                        var optUser = userRepository.findById(ownerId);
                        if (optUser.isPresent()) {
                            ownerName = buildOwnerName(optUser.get().getFirstName(), optUser.get().getLastName());
                        }
                    }
                } else {
                    continue;
                }
            }

            result.add(new TrendingResourceResponseDto(
                    rank++,
                    rId,
                    setId,
                    sr.type,
                    title,
                    description,
                    ownerId,
                    ownerName,
                    sr.score,
                    sr.views,
                    sr.sessions
            ));
        }

        return result;
    }

    @Override
    public List<TopCreatorResponseDto> getTopCreators(TrendingPeriod period, int topN) {
        OffsetDateTime since = period.toSinceDateTime();
        List<Object[]> creators = userRepository.findTopCreators(since, PageRequest.of(0, topN));

        List<TopCreatorResponseDto> result = new ArrayList<>();
        int rank = 1;
        for (Object[] row : creators) {
            Long userId = ((Number) row[0]).longValue();
            String firstName = (String) row[1];
            String lastName = (String) row[2];
            String email = (String) row[3];
            String avatarUrl = (String) row[4];
            long totalResources = ((Number) row[5]).longValue();
            long newResources = ((Number) row[6]).longValue();

            result.add(new TopCreatorResponseDto(
                    rank++,
                    userId,
                    buildOwnerName(firstName, lastName),
                    email,
                    avatarUrl,
                    totalResources,
                    newResources
            ));
        }
        return result;
    }

    @Override
    public List<TopTopicResponseDto> getTopTopics(TrendingPeriod period, int topN) {
        OffsetDateTime since = period.toSinceDateTime();
        List<Object[]> topics = cardItemRepository.findTopTopics(since, PageRequest.of(0, topN));

        List<TopTopicResponseDto> result = new ArrayList<>();
        int rank = 1;
        for (Object[] row : topics) {
            String topic = (String) row[0];
            long totalResources = ((Number) row[1]).longValue();
            long newResources = ((Number) row[2]).longValue();

            result.add(new TopTopicResponseDto(
                    rank++,
                    topic,
                    totalResources,
                    newResources
            ));
        }
        return result;
    }

    private OffsetDateTime toOffsetDateTime(Timestamp ts) {
        return ts != null ? ts.toInstant().atOffset(ZoneOffset.UTC) : null;
    }

    private String buildOwnerName(String firstName, String lastName) {
        return String.format("%s %s",
                Objects.requireNonNullElse(firstName, ""),
                Objects.requireNonNullElse(lastName, "")
        ).trim();
    }
}

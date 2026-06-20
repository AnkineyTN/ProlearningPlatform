package com.cabybara.prolearningplatform.service.roadmap;

import com.cabybara.prolearningplatform.dto.request.roadmap.AcceptRoadmapRequestDto;
import com.cabybara.prolearningplatform.enums.ChapterStatus;
import com.cabybara.prolearningplatform.enums.RoadmapStatus;
import com.cabybara.prolearningplatform.enums.TopicContentStatus;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.roadmap.Roadmap;
import com.cabybara.prolearningplatform.model.roadmap.RoadmapChapter;
import com.cabybara.prolearningplatform.model.roadmap.RoadmapTopic;
import com.cabybara.prolearningplatform.repository.KnowledgeAnalysisRepository;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.repository.RoadmapChapterRepository;
import com.cabybara.prolearningplatform.repository.RoadmapRepository;
import com.cabybara.prolearningplatform.repository.RoadmapTopicRepository;
import com.cabybara.prolearningplatform.repository.SetRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.ai.AIRoadmapService;
import com.cabybara.prolearningplatform.service.roadmap.RoadmapTopicSetupAsyncService;
import com.cabybara.prolearningplatform.service.roadmap.impl.RoadmapServiceImpl;
import com.cabybara.prolearningplatform.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.mockito.MockedStatic;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoadmapServiceImplTest {

    @Mock
    private RoadmapRepository roadmapRepository;

    @Mock
    private RoadmapChapterRepository roadmapChapterRepository;

    @Mock
    private RoadmapTopicRepository roadmapTopicRepository;

    @Mock
    private SetRepository setRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KnowledgeAnalysisRepository knowledgeAnalysisRepository;

    @Mock
    private AIRoadmapService aiRoadmapService;

    @Mock
    private RoadmapTopicSetupAsyncService topicSetupAsyncService;

    @Test
    void acceptRoadmapCreatesRoadmapAndSets() {
        RoadmapServiceImpl service = new RoadmapServiceImpl(
                roadmapRepository, roadmapChapterRepository, roadmapTopicRepository,
                setRepository, noteRepository, userRepository,
                knowledgeAnalysisRepository, aiRoadmapService, topicSetupAsyncService);

        User user = TestFixtures.user(1L);

        AcceptRoadmapRequestDto.TopicDto topicDto = new AcceptRoadmapRequestDto.TopicDto();
        topicDto.setTopicId("topic-1");
        topicDto.setTopicTitle("Test Topic");
        topicDto.setDescription("Topic desc");

        AcceptRoadmapRequestDto.ChapterDto chapterDto = new AcceptRoadmapRequestDto.ChapterDto();
        chapterDto.setChapterId("ch-1");
        chapterDto.setChapterTitle("Test Chapter");
        chapterDto.setObjective("Chapter obj");
        chapterDto.setTopics(List.of(topicDto));

        AcceptRoadmapRequestDto dto = new AcceptRoadmapRequestDto();
        dto.setRoadmapTitle("Test Roadmap");
        dto.setOverview("Test Overview");
        dto.setEstimatedTotalHours(10);
        dto.setChapters(List.of(chapterDto));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(roadmapRepository.save(any(Roadmap.class))).thenAnswer(invocation -> {
            Roadmap r = invocation.getArgument(0);
            r.setId(1L);
            r.setCreatedAt(OffsetDateTime.now());
            return r;
        });

        when(setRepository.save(any(Set.class))).thenAnswer(invocation -> {
            Set s = invocation.getArgument(0);
            s.setId(1L);
            return s;
        });

        when(roadmapChapterRepository.save(any(RoadmapChapter.class))).thenAnswer(invocation -> {
            RoadmapChapter ch = invocation.getArgument(0);
            ch.setId(100L);
            return ch;
        });

        when(roadmapTopicRepository.save(any(RoadmapTopic.class))).thenAnswer(invocation -> {
            RoadmapTopic t = invocation.getArgument(0);
            t.setId(200L);
            return t;
        });

        Roadmap roadmapForQuery = Roadmap.builder()
                .userId(1L).title("Test Roadmap").overview("Test Overview")
                .estimatedTotalHours(10).status(RoadmapStatus.ACTIVE).build();
        roadmapForQuery.setId(1L);
        roadmapForQuery.setCreatedAt(OffsetDateTime.now());

        RoadmapChapter chapterForQuery = RoadmapChapter.builder()
                .roadmap(roadmapForQuery).chapterKey("ch-1").title("Test Chapter")
                .objective("Chapter obj").orderIndex(0).status(ChapterStatus.IN_PROGRESS).build();
        chapterForQuery.setId(1L);

        RoadmapTopic topicForQuery = RoadmapTopic.builder()
                .chapter(chapterForQuery).topicKey("topic-1").title("Test Topic")
                .description("Topic desc").orderIndex(0).completed(false)
                .contentStatus(TopicContentStatus.IDLE).build();
        topicForQuery.setId(1L);

        when(roadmapChapterRepository.findByRoadmapIdOrderByOrderIndex(1L))
                .thenReturn(List.of(chapterForQuery));
        when(roadmapTopicRepository.findAllByRoadmapIdOrdered(1L))
                .thenReturn(List.of(topicForQuery));

        Set setForQuery = Set.builder()
                .title("Test Roadmap").description("Test Overview").user(user)
                .roadmap(roadmapForQuery).build();
        setForQuery.setId(1L);
        when(setRepository.findByRoadmapId(1L)).thenReturn(Optional.of(setForQuery));
        when(noteRepository.findTopicNoteRefsByRoadmapId(1L)).thenReturn(List.of());

        service.acceptRoadmap(1L, dto);

        verify(roadmapRepository).save(any(Roadmap.class));
        verify(setRepository).save(any(Set.class));
        verify(roadmapTopicRepository, times(1)).save(any(RoadmapTopic.class));
    }

    @Test
    void getRoadmapsReturnsOnlyUserRoadmaps() {
        RoadmapServiceImpl service = new RoadmapServiceImpl(
                roadmapRepository, roadmapChapterRepository, roadmapTopicRepository,
                setRepository, noteRepository, userRepository,
                knowledgeAnalysisRepository, aiRoadmapService, topicSetupAsyncService);

        Roadmap r1 = Roadmap.builder()
                .userId(1L).title("R1").overview("Overview 1")
                .estimatedTotalHours(5).status(RoadmapStatus.ACTIVE).build();
        r1.setId(1L);
        r1.setCreatedAt(OffsetDateTime.now());

        Roadmap r2 = Roadmap.builder()
                .userId(1L).title("R2").overview("Overview 2")
                .estimatedTotalHours(3).status(RoadmapStatus.COMPLETED).build();
        r2.setId(2L);
        r2.setCreatedAt(OffsetDateTime.now());

        PageRequest pageable = PageRequest.of(0, 10);
        Page<Roadmap> page = new PageImpl<>(List.of(r1, r2), pageable, 2);

        when(roadmapRepository.findByUserId(1L, pageable)).thenReturn(page);
        when(setRepository.findSetRefsByRoadmapIdIn(anyCollection())).thenReturn(List.of());
        when(roadmapTopicRepository.countByRoadmapId(1L)).thenReturn(5L);
        when(roadmapTopicRepository.countByRoadmapId(2L)).thenReturn(3L);
        when(roadmapTopicRepository.countCompletedByRoadmapId(1L)).thenReturn(2L);
        when(roadmapTopicRepository.countCompletedByRoadmapId(2L)).thenReturn(1L);
        when(roadmapChapterRepository.countByRoadmapId(1L)).thenReturn(3L);
        when(roadmapChapterRepository.countByRoadmapId(2L)).thenReturn(2L);
        when(roadmapChapterRepository.countByRoadmapIdAndStatus(1L, ChapterStatus.COMPLETED)).thenReturn(1L);
        when(roadmapChapterRepository.countByRoadmapIdAndStatus(2L, ChapterStatus.COMPLETED)).thenReturn(0L);

        var result = service.getRoadmaps(1L, pageable);

        assertEquals(2, result.getContent().size());
    }

    @Test
    void startTopicMarksGenerating() {
        RoadmapServiceImpl service = new RoadmapServiceImpl(
                roadmapRepository, roadmapChapterRepository, roadmapTopicRepository,
                setRepository, noteRepository, userRepository,
                knowledgeAnalysisRepository, aiRoadmapService, topicSetupAsyncService);

        Roadmap roadmap = Roadmap.builder()
                .userId(1L).title("Test").overview("Overview")
                .status(RoadmapStatus.ACTIVE).build();
        roadmap.setId(1L);

        RoadmapChapter chapter = RoadmapChapter.builder()
                .roadmap(roadmap).chapterKey("ch-1").title("Chapter 1")
                .orderIndex(0).status(ChapterStatus.IN_PROGRESS).build();
        chapter.setId(1L);

        RoadmapTopic topic = RoadmapTopic.builder()
                .chapter(chapter).topicKey("topic-1").title("Topic 1")
                .orderIndex(0).completed(false).contentStatus(TopicContentStatus.IDLE).build();
        topic.setId(1L);

        Set set = Set.builder()
                .title("Test Roadmap").user(TestFixtures.user(1L)).build();
        set.setId(1L);

        when(roadmapRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(roadmap));
        when(roadmapTopicRepository.findById(1L)).thenReturn(Optional.of(topic));
        when(setRepository.findByRoadmapId(1L)).thenReturn(Optional.of(set));
        when(roadmapTopicRepository.save(any(RoadmapTopic.class))).thenReturn(topic);

        try (MockedStatic<TransactionSynchronizationManager> tsm =
                     mockStatic(TransactionSynchronizationManager.class)) {
            var result = service.startTopic(1L, 1L, 1L);

            assertEquals(TopicContentStatus.GENERATING, topic.getContentStatus());
            verify(roadmapTopicRepository).save(topic);
            assertNotNull(result);
        }
    }

    @Test
    void abandonRoadmapMarksAbandoned() {
        RoadmapServiceImpl service = new RoadmapServiceImpl(
                roadmapRepository, roadmapChapterRepository, roadmapTopicRepository,
                setRepository, noteRepository, userRepository,
                knowledgeAnalysisRepository, aiRoadmapService, topicSetupAsyncService);

        Roadmap roadmap = Roadmap.builder()
                .userId(1L).title("Test").overview("Overview")
                .status(RoadmapStatus.ACTIVE).build();
        roadmap.setId(1L);

        when(roadmapRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(roadmap));

        service.abandonRoadmap(1L, 1L);

        assertEquals(RoadmapStatus.ABANDONED, roadmap.getStatus());
        verify(roadmapRepository).save(roadmap);
    }
}

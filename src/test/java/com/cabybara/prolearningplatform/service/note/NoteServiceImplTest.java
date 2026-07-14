package com.cabybara.prolearningplatform.service.note;

import com.cabybara.prolearningplatform.dto.request.note.CreateNoteRequestDTO;
import com.cabybara.prolearningplatform.dto.request.note.GenerateNoteWithAIRequestDTO;
import com.cabybara.prolearningplatform.dto.request.note.SaveNoteRequestDTO;
import com.cabybara.prolearningplatform.dto.response.note.CreateNoteResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.GenerateNoteWithAIResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.GetDetailNoteResponseDTO;
import com.cabybara.prolearningplatform.enums.ContentType;
import com.cabybara.prolearningplatform.enums.NoteRole;
import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.model.Asset;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.note.Note;
import com.cabybara.prolearningplatform.model.note.NoteDocs;
import com.cabybara.prolearningplatform.model.note.NoteImgs;
import com.cabybara.prolearningplatform.repository.AssetRepository;
import com.cabybara.prolearningplatform.repository.NoteDocsRepository;
import com.cabybara.prolearningplatform.repository.NoteImgsRepository;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.repository.SetRepository;
import com.cabybara.prolearningplatform.repository.UserFavoriteResourceRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.ai.AINoteService;
import com.cabybara.prolearningplatform.service.asset.AssetService;
import com.cabybara.prolearningplatform.service.note.NoteFileRegionCommentService;
import com.cabybara.prolearningplatform.service.note.impl.NoteServiceImpl;
import com.cabybara.prolearningplatform.service.permission.impl.NotePermissionService;
import com.cabybara.prolearningplatform.support.TestFixtures;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

    @Mock
    private SetRepository setRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private NoteDocsRepository noteDocsRepository;

    @Mock
    private NoteImgsRepository noteImgsRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AssetService assetService;

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private NoteFileRegionCommentService noteFileRegionCommentService;

    @Mock
    private NotePermissionService notePermissionService;

    @Mock
    private AINoteService aiNoteService;

    @Mock
    private UserFavoriteResourceRepository userFavoriteResourceRepository;

    @Test
    void createNoteReturnsNoteDto() {
        NoteServiceImpl service = new NoteServiceImpl(setRepository, noteRepository, noteDocsRepository,
                noteImgsRepository, assetRepository, userRepository, assetService, authenticationContext,
                noteFileRegionCommentService, notePermissionService, aiNoteService, userFavoriteResourceRepository);

        User user = TestFixtures.user(1L);
        Set set = Set.builder().title("My Set").user(user).build();
        set.setId(100L);

        CreateNoteRequestDTO request = new CreateNoteRequestDTO();
        request.setTitle("My Note");
        request.setPrivacy(Privacy.PUBLIC);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(setRepository.findById(100L)).thenReturn(Optional.of(set));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> {
            Note note = invocation.getArgument(0);
            note.setId(200L);
            return note;
        });

        CreateNoteResponseDTO response = service.createNote(100L, request);

        ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(noteCaptor.capture());
        Note saved = noteCaptor.getValue();
        assertEquals("My Note", saved.getTitle());
        assertEquals(Privacy.PUBLIC, saved.getPrivacy());
        assertEquals(set, saved.getSet());
        assertEquals(user, saved.getUser());
        assertEquals(200L, response.getNoteId());
        verify(setRepository).updateLastModifiedDate(any(Long.class), any(java.time.OffsetDateTime.class));
    }

    @Test
    void saveNoteUpdatesContent() {
        NoteServiceImpl service = new NoteServiceImpl(setRepository, noteRepository, noteDocsRepository,
                noteImgsRepository, assetRepository, userRepository, assetService, authenticationContext,
                noteFileRegionCommentService, notePermissionService, aiNoteService, userFavoriteResourceRepository);

        Note note = Note.builder()
                .title("Old Title")
                .content("Old Content")
                .build();
        note.setId(1L);

        SaveNoteRequestDTO request = new SaveNoteRequestDTO();
        request.setTitle("New Title");
        request.setContent("updated content");

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(noteRepository.findByIdAndSetId(1L, 1L)).thenReturn(Optional.of(note));
        when(notePermissionService.canEdit(1L, 1L)).thenReturn(true);
        when(noteRepository.save(any(Note.class))).thenReturn(note);

        service.saveNote(1L, 1L, request);

        assertEquals("New Title", note.getTitle());
        assertEquals("updated content", note.getContent());
        verify(noteRepository).save(note);
    }

    @Test
    void deleteNoteRemovesDocsAndImgs() {
        NoteServiceImpl service = new NoteServiceImpl(setRepository, noteRepository, noteDocsRepository,
                noteImgsRepository, assetRepository, userRepository, assetService, authenticationContext,
                noteFileRegionCommentService, notePermissionService, aiNoteService, userFavoriteResourceRepository);

        User user = TestFixtures.user(1L);
        Asset docAsset = Asset.builder().fileName("doc.pdf").url("http://example.com/doc.pdf").publicId("pub-1").build();
        docAsset.setId(100L);
        Asset imgAsset = Asset.builder().fileName("img.png").url("http://example.com/img.png").publicId("pub-2").build();
        imgAsset.setId(200L);

        Note note = Note.builder()
                .title("Note to delete")
                .user(user)
                .noteDocs(new ArrayList<>())
                .noteImgs(new ArrayList<>())
                .build();
        note.setId(1L);

        NoteDocs noteDoc = new NoteDocs(note, docAsset);
        NoteImgs noteImg = new NoteImgs(note, imgAsset);
        note.getNoteDocs().add(noteDoc);
        note.getNoteImgs().add(noteImg);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(noteRepository.findByIdAndUserIdAndSetId(1L, 1L, 1L)).thenReturn(Optional.of(note));
        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));
        when(notePermissionService.canEdit(1L, 1L)).thenReturn(true);
        when(assetRepository.findById(100L)).thenReturn(Optional.of(docAsset));
        when(assetRepository.findById(200L)).thenReturn(Optional.of(imgAsset));

        service.deleteNote(1L, 1L);

        verify(noteDocsRepository).deleteById(any(com.cabybara.prolearningplatform.model.composite_key.NoteDocsId.class));
        verify(noteImgsRepository).deleteById(any(com.cabybara.prolearningplatform.model.composite_key.NoteImgsId.class));
        verify(noteRepository).delete(note);
    }

    @Test
    void getDetailNoteIncludesDocsImgsAndRole() {
        NoteServiceImpl service = new NoteServiceImpl(setRepository, noteRepository, noteDocsRepository,
                noteImgsRepository, assetRepository, userRepository, assetService, authenticationContext,
                noteFileRegionCommentService, notePermissionService, aiNoteService, userFavoriteResourceRepository);

        User user = TestFixtures.user(1L);
        Asset docAsset = Asset.builder().fileName("doc.pdf").url("http://example.com/doc.pdf").publicId("pub-doc").build();
        docAsset.setId(100L);
        Asset imgAsset = Asset.builder().fileName("img.png").url("http://example.com/img.png").publicId("pub-img").build();
        imgAsset.setId(200L);

        Note note = Note.builder()
                .title("Detail Note")
                .description("A note with docs and imgs")
                .privacy(Privacy.PUBLIC)
                .content("Some content")
                .user(user)
                .noteDocs(new ArrayList<>())
                .noteImgs(new ArrayList<>())
                .build();
        note.setId(1L);

        Set set = Set.builder().title("Test Set").build();
        set.setId(10L);
        note.setSet(set);

        NoteDocs noteDoc = new NoteDocs(note, docAsset);
        NoteImgs noteImg = new NoteImgs(note, imgAsset);
        note.getNoteDocs().add(noteDoc);
        note.getNoteImgs().add(noteImg);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(noteRepository.findByIdAndSetId(1L, 10L)).thenReturn(Optional.of(note));
        when(notePermissionService.getUserRoleInNote(1L, 1L)).thenReturn(NoteRole.OWNER);
        when(userFavoriteResourceRepository.existsByUserIdAndResourceIdAndResourceType(1L, 1L, ContentType.NOTE)).thenReturn(true);

        GetDetailNoteResponseDTO response = service.getDetailNote(10L, 1L);

        assertEquals(1L, response.getId());
        assertEquals(10L, response.getSetId());
        assertEquals("Detail Note", response.getTitle());
        assertEquals(Privacy.PUBLIC, response.getPrivacy());
        assertEquals("Some content", response.getContent());
        assertEquals(NoteRole.OWNER, response.getUserRole());
        assertTrue(response.getIsFavorited());
        assertNotNull(response.getNoteDocs());
        assertEquals(1, response.getNoteDocs().size());
        assertEquals(100L, response.getNoteDocs().get(0).getAssetId());
        assertNotNull(response.getNoteImgs());
        assertEquals(1, response.getNoteImgs().size());
        assertEquals(200L, response.getNoteImgs().get(0).getAssetId());
        assertEquals(1L, response.getOwnerId());
    }

    @Test
    void createNoteWithAICallsAINoteService() {
        NoteServiceImpl service = new NoteServiceImpl(setRepository, noteRepository, noteDocsRepository,
                noteImgsRepository, assetRepository, userRepository, assetService, authenticationContext,
                noteFileRegionCommentService, notePermissionService, aiNoteService, userFavoriteResourceRepository);

        User user = TestFixtures.user(1L);
        Set set = Set.builder().title("My Set").user(user).build();
        set.setId(100L);

        GenerateNoteWithAIRequestDTO request = new GenerateNoteWithAIRequestDTO();
        request.setTopic("Write about Java");
        request.setPrivacy(Privacy.PUBLIC);

        GenerateNoteWithAIResponseDTO aiResponse = GenerateNoteWithAIResponseDTO.builder()
                .title("AI Generated: Java Overview")
                .content("Java is a popular programming language...")
                .build();

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(setRepository.findById(100L)).thenReturn(Optional.of(set));
        when(aiNoteService.generateNoteContent(request)).thenReturn(aiResponse);
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> {
            Note note = invocation.getArgument(0);
            note.setId(300L);
            return note;
        });

        service.createNoteWithAI(100L, request);

        ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
        verify(aiNoteService).generateNoteContent(request);
        verify(noteRepository).save(noteCaptor.capture());
        Note saved = noteCaptor.getValue();
        assertEquals("AI Generated: Java Overview", saved.getTitle());
        assertEquals("Java is a popular programming language...", saved.getContent());
        assertEquals(set, saved.getSet());
        assertEquals(user, saved.getUser());
    }
}

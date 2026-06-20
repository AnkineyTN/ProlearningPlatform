package com.cabybara.prolearningplatform.service.note;

import com.cabybara.prolearningplatform.dto.request.note.CreateNoteFileRegionCommentRequestDTO;
import com.cabybara.prolearningplatform.dto.request.note.RectPercentRequestDTO;
import com.cabybara.prolearningplatform.dto.response.note.NoteFileRegionCommentResponseDTO;
import com.cabybara.prolearningplatform.enums.AssetType;
import com.cabybara.prolearningplatform.model.Asset;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.note.Note;
import com.cabybara.prolearningplatform.model.note.NoteFileRegionComment;
import com.cabybara.prolearningplatform.repository.AssetRepository;
import com.cabybara.prolearningplatform.repository.NoteDocsRepository;
import com.cabybara.prolearningplatform.repository.NoteFileRegionCommentRepository;
import com.cabybara.prolearningplatform.repository.NoteImgsRepository;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.asset.AssetService;
import com.cabybara.prolearningplatform.service.note.impl.NoteFileRegionCommentServiceImpl;
import com.cabybara.prolearningplatform.support.TestFixtures;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteFileRegionCommentServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private NoteDocsRepository noteDocsRepository;

    @Mock
    private NoteImgsRepository noteImgsRepository;

    @Mock
    private NoteFileRegionCommentRepository commentRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AssetService assetService;

    @Mock
    private AuthenticationContext authenticationContext;

    @Test
    void createCommentOnRegion() {
        NoteFileRegionCommentServiceImpl service = new NoteFileRegionCommentServiceImpl(noteRepository,
                noteDocsRepository, noteImgsRepository, commentRepository, assetRepository, userRepository,
                assetService, authenticationContext);

        User user = TestFixtures.user(1L);
        Note note = Note.builder().title("Test Note").user(user).build();
        note.setId(1L);
        Asset asset = Asset.builder().fileName("test.pdf").url("http://example.com/test.pdf")
                .publicId("pub-test").type(AssetType.DOCUMENT).user(user).build();
        asset.setId(1L);

        RectPercentRequestDTO rect = RectPercentRequestDTO.builder()
                .x(10.0).y(20.0).width(30.0).height(40.0).build();

        CreateNoteFileRegionCommentRequestDTO request = CreateNoteFileRegionCommentRequestDTO.builder()
                .noteAssetId(1L)
                .kind("doc")
                .pageNumber(1)
                .rectPercent(rect)
                .content("Good point")
                .build();

        NoteFileRegionComment comment = NoteFileRegionComment.builder()
                .note(note)
                .asset(asset)
                .content("Good point")
                .build();
        comment.setId(100L);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(noteRepository.findByIdAndUserIdAndSetId(1L, 1L, 1L)).thenReturn(Optional.of(note));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(noteDocsRepository.existsById(any())).thenReturn(true);
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(commentRepository.save(any(NoteFileRegionComment.class))).thenReturn(comment);

        NoteFileRegionCommentResponseDTO response = service.create(1L, 1L, request);

        verify(commentRepository).save(any(NoteFileRegionComment.class));
        assertEquals(100L, response.getId());
    }

    @Test
    void listCommentsForAsset() {
        NoteFileRegionCommentServiceImpl service = new NoteFileRegionCommentServiceImpl(noteRepository,
                noteDocsRepository, noteImgsRepository, commentRepository, assetRepository, userRepository,
                assetService, authenticationContext);

        User user = TestFixtures.user(1L);
        Note note = Note.builder().title("Test Note").user(user).build();
        note.setId(1L);
        Asset asset = Asset.builder().fileName("test.pdf").url("http://example.com/test.pdf")
                .publicId("pub-test").type(AssetType.DOCUMENT).user(user).build();
        asset.setId(1L);

        NoteFileRegionComment comment1 = NoteFileRegionComment.builder()
                .note(note).asset(asset).content("First comment").build();
        comment1.setId(101L);
        NoteFileRegionComment comment2 = NoteFileRegionComment.builder()
                .note(note).asset(asset).content("Second comment").build();
        comment2.setId(102L);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(noteRepository.findByIdAndUserIdAndSetId(1L, 1L, 1L)).thenReturn(Optional.of(note));
        when(commentRepository.findByNote_IdAndAsset_IdOrderByCreatedAtAsc(1L, 1L))
                .thenReturn(List.of(comment1, comment2));

        List<NoteFileRegionCommentResponseDTO> result = service.list(1L, 1L, 1L);

        assertEquals(2, result.size());
    }

    @Test
    void deleteRemovesComment() {
        NoteFileRegionCommentServiceImpl service = new NoteFileRegionCommentServiceImpl(noteRepository,
                noteDocsRepository, noteImgsRepository, commentRepository, assetRepository, userRepository,
                assetService, authenticationContext);

        User user = TestFixtures.user(1L);
        Note note = Note.builder().title("Test Note").user(user).build();
        note.setId(1L);
        Asset asset = Asset.builder().fileName("test.pdf").url("http://example.com/test.pdf")
                .publicId("pub-test").type(AssetType.DOCUMENT).user(user).build();
        asset.setId(1L);

        NoteFileRegionComment comment = NoteFileRegionComment.builder()
                .note(note).asset(asset).content("To be deleted").build();
        comment.setId(1L);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(noteRepository.findByIdAndUserIdAndSetId(1L, 1L, 1L)).thenReturn(Optional.of(note));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        service.delete(1L, 1L, 1L);

        verify(commentRepository).delete(comment);
    }
}

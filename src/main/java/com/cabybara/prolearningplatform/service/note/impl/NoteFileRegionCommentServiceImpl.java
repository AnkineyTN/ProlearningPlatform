package com.cabybara.prolearningplatform.service.note.impl;

import com.cabybara.prolearningplatform.dto.request.note.CreateNoteFileRegionCommentRequestDTO;
import com.cabybara.prolearningplatform.dto.response.note.NoteFileRegionCommentResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.RectPercentResponseDTO;
import com.cabybara.prolearningplatform.enums.NoteFileAttachmentKind;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.Asset;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.composite_key.NoteDocsId;
import com.cabybara.prolearningplatform.model.composite_key.NoteImgsId;
import com.cabybara.prolearningplatform.model.note.Note;
import com.cabybara.prolearningplatform.model.note.NoteFileRegionComment;
import com.cabybara.prolearningplatform.repository.AssetRepository;
import com.cabybara.prolearningplatform.repository.NoteDocsRepository;
import com.cabybara.prolearningplatform.repository.NoteFileRegionCommentRepository;
import com.cabybara.prolearningplatform.repository.NoteImgsRepository;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.note.NoteFileRegionCommentService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class NoteFileRegionCommentServiceImpl implements NoteFileRegionCommentService {

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final NoteRepository noteRepository;
    private final NoteDocsRepository noteDocsRepository;
    private final NoteImgsRepository noteImgsRepository;
    private final NoteFileRegionCommentRepository commentRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final AuthenticationContext authenticationContext;

    @Override
    @Transactional
    public NoteFileRegionCommentResponseDTO create(
            Long setId,
            Long noteId,
            CreateNoteFileRegionCommentRequestDTO request
    ) {
        Long userId = authenticationContext.getCurrentUserId();
        Note note = noteRepository.findByIdAndUserIdAndSetId(noteId, userId, setId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found or no permission"));
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        NoteFileAttachmentKind kind = parseKind(request.getKind());
        assertAssetLinkedToNote(noteId, request.getNoteAssetId(), kind);

        Asset asset = assetRepository.findById(request.getNoteAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

        if (request.getPublicId() != null && !request.getPublicId().isBlank()
                && !request.getPublicId().equals(asset.getPublicId())) {
            throw new IllegalArgumentException("publicId does not match asset");
        }

        var r = request.getRectPercent();
        NoteFileRegionComment entity = NoteFileRegionComment.builder()
                .note(note)
                .asset(asset)
                .attachmentKind(kind)
                .pageNumber(request.getPageNumber())
                .rectX(r.getX())
                .rectY(r.getY())
                .rectWidth(r.getWidth())
                .rectHeight(r.getHeight())
                .content(request.getContent().trim())
                .clientCommentId(trimToNull(request.getClientCommentId()))
                .author(author)
                .build();

        NoteFileRegionComment saved = commentRepository.save(entity);
        log.info("Saved file region comment id {} on note {} asset {}", saved.getId(), noteId, asset.getId());
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteFileRegionCommentResponseDTO> list(Long setId, Long noteId, Long assetId) {
        Long userId = authenticationContext.getCurrentUserId();
        noteRepository.findByIdAndUserIdAndSetId(noteId, userId, setId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found or no permission"));

        List<NoteFileRegionComment> rows = assetId == null
                ? commentRepository.findByNote_IdOrderByCreatedAtAsc(noteId)
                : commentRepository.findByNote_IdAndAsset_IdOrderByCreatedAtAsc(noteId, assetId);
        return rows.stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public void delete(Long setId, Long noteId, Long commentId) {
        Long userId = authenticationContext.getCurrentUserId();
        Note note = noteRepository.findByIdAndUserIdAndSetId(noteId, userId, setId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found or no permission"));
        NoteFileRegionComment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        if (!c.getNote().getId().equals(note.getId())) {
            throw new ResourceNotFoundException("Comment not found");
        }
        commentRepository.delete(c);
        log.info("Deleted file region comment {}", commentId);
    }

    @Override
    @Transactional
    public void deleteAllForNoteAndAsset(Long noteId, Long assetId) {
        commentRepository.deleteByNote_IdAndAsset_Id(noteId, assetId);
    }

    private void assertAssetLinkedToNote(Long noteId, Long assetId, NoteFileAttachmentKind kind) {
        if (kind == NoteFileAttachmentKind.DOC) {
            if (!noteDocsRepository.existsById(new NoteDocsId(noteId, assetId))) {
                throw new IllegalArgumentException("Document is not attached to this note");
            }
        } else {
            if (!noteImgsRepository.existsById(new NoteImgsId(noteId, assetId))) {
                throw new IllegalArgumentException("Image is not attached to this note");
            }
        }
    }

    private static NoteFileAttachmentKind parseKind(String raw) {
        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "doc" -> NoteFileAttachmentKind.DOC;
            case "image" -> NoteFileAttachmentKind.IMAGE;
            default -> throw new IllegalArgumentException("kind must be doc or image");
        };
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private NoteFileRegionCommentResponseDTO toDto(NoteFileRegionComment c) {
        return NoteFileRegionCommentResponseDTO.builder()
                .id(c.getId())
                .noteId(c.getNote().getId())
                .noteAssetId(c.getAsset().getId())
                .kind(c.getAttachmentKind() == NoteFileAttachmentKind.DOC ? "doc" : "image")
                .pageNumber(c.getPageNumber())
                .rectPercent(RectPercentResponseDTO.builder()
                        .x(c.getRectX())
                        .y(c.getRectY())
                        .width(c.getRectWidth())
                        .height(c.getRectHeight())
                        .build())
                .content(c.getContent())
                .clientCommentId(c.getClientCommentId())
                .createdAt(c.getCreatedAt() != null ? ISO_FMT.format(c.getCreatedAt()) : null)
                .updatedAt(c.getUpdatedAt() != null ? ISO_FMT.format(c.getUpdatedAt()) : null)
                .build();
    }
}

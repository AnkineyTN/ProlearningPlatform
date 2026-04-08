package com.cabybara.prolearningplatform.service.note;

import com.cabybara.prolearningplatform.dto.request.note.CreateNoteFileRegionCommentRequestDTO;
import com.cabybara.prolearningplatform.dto.response.note.NoteFileRegionCommentResponseDTO;

import java.util.List;

public interface NoteFileRegionCommentService {

    NoteFileRegionCommentResponseDTO create(Long setId, Long noteId, CreateNoteFileRegionCommentRequestDTO request);

    List<NoteFileRegionCommentResponseDTO> list(Long setId, Long noteId, Long assetId);

    void delete(Long setId, Long noteId, Long commentId);

    void deleteAllForNoteAndAsset(Long noteId, Long assetId);
}

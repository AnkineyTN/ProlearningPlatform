package com.cabybara.prolearningplatform.service.note;

import com.cabybara.prolearningplatform.dto.request.*;
import com.cabybara.prolearningplatform.dto.response.GetDetailNoteResponseDTO;
import com.cabybara.prolearningplatform.dto.response.PageResponseDetail;

import java.io.IOException;

public interface NoteService {
    public void createNote(CreateNoteRequestDTO request);

    public void saveNote(Long noteId, SaveNoteRequestDTO request);

    public PageResponseDetail<?> getAllNotesOfSet(int pageNo, int pageSize, Long setId);

    public GetDetailNoteResponseDTO getDetailNote(Long noteId);

    public void deleteDocInNote(Long noteDocsId, DeleteNoteDocRequestDTO request) throws IOException;

    public void deleteImgInNote(DeleteNoteImgRequestDTO request) throws IOException;

    public void updateNote(Long noteId, UpdateNoteRequestDTO request);

    public void deleteNote(Long noteId) throws IOException;
}

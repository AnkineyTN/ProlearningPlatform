package com.cabybara.prolearningplatform.service;

import com.cabybara.prolearningplatform.dto.request.CreateNoteRequestDTO;
import com.cabybara.prolearningplatform.dto.request.SaveNoteRequestDTO;
import com.cabybara.prolearningplatform.dto.response.GetDetailNoteResponseDTO;
import com.cabybara.prolearningplatform.dto.response.PageResponseDetail;

public interface NoteService {
    public void createNote(CreateNoteRequestDTO request);

    public void saveNote(Long noteId, SaveNoteRequestDTO request);

    public PageResponseDetail<?> getAllNotesOfSet(int pageNo, int pageSize, Long setId);

    public GetDetailNoteResponseDTO getDetailNote(Long noteId);
}

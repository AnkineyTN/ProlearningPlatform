package com.cabybara.prolearningplatform.service;

import com.cabybara.prolearningplatform.dto.request.CreateNoteRequestDTO;
import com.cabybara.prolearningplatform.dto.request.SaveNoteRequestDTO;

public interface NoteService {
    public void createNote(CreateNoteRequestDTO request);

    public void saveNote(Long noteId, SaveNoteRequestDTO request);
}

package com.cabybara.prolearningplatform.service;

import com.cabybara.prolearningplatform.dto.request.CreateNoteRequestDTO;

public interface NoteService {
    public void createNote(CreateNoteRequestDTO request);
}

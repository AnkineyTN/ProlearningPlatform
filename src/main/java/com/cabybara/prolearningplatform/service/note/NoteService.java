package com.cabybara.prolearningplatform.service.note;

import com.cabybara.prolearningplatform.dto.request.*;
import com.cabybara.prolearningplatform.dto.request.note.DeleteNoteDocRequestDTO;
import com.cabybara.prolearningplatform.dto.request.note.SaveDocInNoteRequestDto;
import com.cabybara.prolearningplatform.dto.request.note.SaveImgInNoteRequestDto;
import com.cabybara.prolearningplatform.dto.response.CreateNoteResponseDTO;
import com.cabybara.prolearningplatform.dto.response.GetDetailNoteResponseDTO;
import com.cabybara.prolearningplatform.dto.response.PageResponseDetail;

import java.io.IOException;

public interface NoteService {
    public CreateNoteResponseDTO createNote(CreateNoteRequestDTO request);

    public void saveNote(Long noteId, SaveNoteRequestDTO request);

    public void saveDocInNote(SaveDocInNoteRequestDto request);

    public void saveImgInNote(SaveImgInNoteRequestDto request);

    public PageResponseDetail<?> getAllNotesOfSet(int pageNo, int pageSize, Long setId);

    public GetDetailNoteResponseDTO getDetailNote(Long noteId);

    public void deleteDocInNote(DeleteNoteDocRequestDTO request);

    public void deleteImgInNote(DeleteNoteImgRequestDTO request) throws IOException;

    public void updateNote(Long noteId, UpdateNoteRequestDTO request);

    public void deleteNote(Long noteId) throws IOException;
}

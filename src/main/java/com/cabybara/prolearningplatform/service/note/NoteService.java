package com.cabybara.prolearningplatform.service.note;

import com.cabybara.prolearningplatform.dto.request.note.*;
import com.cabybara.prolearningplatform.dto.response.note.CreateNoteResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.GetAllNotesResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.GetDetailNoteResponseDTO;
import com.cabybara.prolearningplatform.dto.response.PageResponseDetail;
import com.cabybara.prolearningplatform.enums.Privacy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

public interface NoteService {
    public CreateNoteResponseDTO createNote(Long setId, CreateNoteRequestDTO request);

    public void saveNote(Long setId, Long noteId, SaveNoteRequestDTO request);

    public void saveDocInNote(Long setId, SaveDocInNoteRequestDto request);

    public void saveImgInNote(SaveImgInNoteRequestDto request);

    public PageResponseDetail<?> getAllNotesOfSet(int pageNo, int pageSize, Long setId);

    public GetDetailNoteResponseDTO getDetailNote(Long noteId);

    public void deleteDocInNote(DeleteNoteDocRequestDTO request);

    public void deleteImgInNote(DeleteNoteImgRequestDTO request) throws IOException;

    public void updateNote(Long noteId, UpdateNoteRequestDTO request);

    public void deleteNote(Long noteId) throws IOException;

    Page<GetAllNotesResponseDTO> getAllNotes(Long setId, String q, Privacy privacy, Pageable pageable);
}

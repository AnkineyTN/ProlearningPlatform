package com.cabybara.prolearningplatform.service.note;

import com.cabybara.prolearningplatform.dto.request.note.*;
import com.cabybara.prolearningplatform.dto.request.share.InviteMemberRequest;
import com.cabybara.prolearningplatform.dto.response.note.CreateNoteResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.GetAllNotesResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.GetDetailNoteResponseDTO;
import com.cabybara.prolearningplatform.dto.response.share.InviteResultResponse;
import com.cabybara.prolearningplatform.dto.response.share.PendingInviteResponse;
import com.cabybara.prolearningplatform.dto.response.PageResponseDetail;
import com.cabybara.prolearningplatform.enums.Privacy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface NoteService {
    public CreateNoteResponseDTO createNote(Long setId, CreateNoteRequestDTO request);

    public void saveNote(Long setId, Long noteId, SaveNoteRequestDTO request);

    public void saveDocInNote(Long setId, SaveDocInNoteRequestDto request);

    public void deleteDocInNote(DeleteNoteDocRequestDTO request);

    public void saveImgInNote(Long setId, SaveImgInNoteRequestDto request);

    public void deleteImgInNote(DeleteNoteImgRequestDTO request) throws IOException;

    Page<GetAllNotesResponseDTO> getAllNotes(Long setId, String q, Privacy privacy, Pageable pageable);

    public GetDetailNoteResponseDTO getDetailNote(Long setId, Long noteId);

    public void updateNote(Long setId, Long noteId, UpdateNoteRequestDTO request);

    public void deleteNote(Long setId, Long noteId) throws IOException;

    public List<InviteResultResponse> inviteMembers(Long setId, Long noteId, InviteMemberRequest request);

    public List<PendingInviteResponse> getPendingInvites();

    public void acceptInvite(Long noteId);
    
    public void declineInvite(Long noteId);

    public void removeMember(Long noteId, Long targetUserId);
}

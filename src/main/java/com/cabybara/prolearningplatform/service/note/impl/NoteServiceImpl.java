package com.cabybara.prolearningplatform.service.note.impl;

import com.cabybara.prolearningplatform.dto.request.note.*;
import com.cabybara.prolearningplatform.dto.request.share.InviteMemberRequest;
import com.cabybara.prolearningplatform.dto.response.*;
import com.cabybara.prolearningplatform.dto.response.note.AcceptByTokenResponse;
import com.cabybara.prolearningplatform.dto.response.note.CreateNoteResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.GetAllNotesResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.GetDetailNoteResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.GetDocsInNoteResponseDTO;
import com.cabybara.prolearningplatform.dto.response.share.InviteResultResponse;
import com.cabybara.prolearningplatform.dto.response.share.PendingInviteResponse;
import com.cabybara.prolearningplatform.enums.NoteRole;
import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.*;
import com.cabybara.prolearningplatform.model.composite_key.NoteDocsId;
import com.cabybara.prolearningplatform.model.composite_key.NoteImgsId;
import com.cabybara.prolearningplatform.model.note.Note;
import com.cabybara.prolearningplatform.model.note.NoteDocs;
import com.cabybara.prolearningplatform.model.note.NoteImgs;
import com.cabybara.prolearningplatform.repository.*;
import com.cabybara.prolearningplatform.service.asset.AssetService;
import com.cabybara.prolearningplatform.service.note.NoteFileRegionCommentService;
import com.cabybara.prolearningplatform.service.note.NoteService;
import com.cabybara.prolearningplatform.service.permission.impl.NotePermissionService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {
    
    // ##################################################
    // #################  PREPARATION  ##################
    // ##################################################

    private final SetRepository setRepository;
    private final NoteRepository noteRepository;
    private final NoteDocsRepository noteDocsRepository;
    private final NoteImgsRepository noteImgsRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    private final AssetService assetService;
    private final AuthenticationContext authenticationContext;
    private final NoteFileRegionCommentService noteFileRegionCommentService;
    private final NotePermissionService notePermissionService;

    // ##################################################
    // #################  MAIN METHOD  ##################
    // ##################################################

    // [POST]: /sets/{setId}/notes
    @Override
    public CreateNoteResponseDTO createNote(Long setId, CreateNoteRequestDTO request) {
        Long userId = authenticationContext.getCurrentUserId();
        User user = getUserById(userId);
        Set set = getSetByIdAndUserId(setId, userId);

        Note note = Note.builder()
                .title(request.getTitle())
                .privacy(request.getPrivacy())
                .description(request.getDescription())
                .set(set)
                .user(user)
                .build();
        Note saved = noteRepository.save(note);
        log.info("✅ Created note '{}' in set id {} by user {}", note.getTitle(), set.getId(), userId);

        notePermissionService.addOwner(saved.getId(), userId);

        return CreateNoteResponseDTO.builder()
                .noteId(saved.getId())
                .build();
    }

    // [PATCH]: /sets/{setId}/notes/save
    @Override
    public void saveNote(Long setId, Long noteId, SaveNoteRequestDTO request) {
        Long userId = authenticationContext.getCurrentUserId();

        Note note = getNoteByIdAndUserIdAndSetId(noteId, userId, setId);

        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        noteRepository.save(note);
        log.info("✅ Save note with noteId '{}'", noteId);
    }

    // [POST]: /sets/{setId}/notes/save-doc
    @Override
    public void saveDocInNote(Long setId, SaveDocInNoteRequestDto request) {
        Long userId = authenticationContext.getCurrentUserId();
        Note note = getNoteByIdAndUserIdAndSetId(request.getNoteId(), userId, setId);
        Asset asset = getAssetById(request.getAssetId());

        NoteDocs noteDocs = new NoteDocs(note, asset);

        noteDocsRepository.save(noteDocs);
        log.info("✅ Save document in note with noteId {} and assetId {}", note.getTitle(), asset.getId());
    }

    // [DELETE]: /sets/{setId}/notes/delete-doc
    @Override
    public void deleteDocInNote(DeleteNoteDocRequestDTO request) {
        noteFileRegionCommentService.deleteAllForNoteAndAsset(request.getNoteId(), request.getAssetId());
        // Mark status "DELETED" in asset table
        Asset asset = getAssetById(request.getAssetId());
        assetService.markDeletedAsset(asset);

        // Delete from note_docs table
        NoteDocsId noteDocsId = new NoteDocsId(request.getNoteId(), request.getAssetId());
        noteDocsRepository.deleteById(noteDocsId);

        log.info("✅ Delete doc in note with noteId {} and assetId {}", request.getNoteId(), request.getAssetId());
    }

    // [POST]: /sets/{setId}/notes/save-img
    @Override
    public void saveImgInNote(Long setId, SaveImgInNoteRequestDto request) {
        Long userId = authenticationContext.getCurrentUserId();
        Note note = getNoteByIdAndUserIdAndSetId(request.getNoteId(), userId, setId);
        Asset asset = getAssetById(request.getAssetId());

        NoteImgs noteImgs = new NoteImgs(note, asset);

        noteImgsRepository.save(noteImgs);
        log.info("✅ Save image in note with noteId {} and assetId {}", note.getTitle(), asset.getId());
    }

    // [DELETE]: /sets/{setId}/notes/delete-img
    @Override
    public void deleteImgInNote(DeleteNoteImgRequestDTO request) {
        Asset asset = assetRepository.findByUrl(request.getFileUrl());
        noteFileRegionCommentService.deleteAllForNoteAndAsset(request.getNoteId(), asset.getId());
        // Mark status "DELETED" in asset table
        assetService.markDeletedAsset(asset);

        // Delete from note_imgs table
        NoteImgsId noteImgsId = new NoteImgsId(request.getNoteId(), asset.getId());
        noteImgsRepository.deleteById(noteImgsId);

        log.info("✅ Delete img in note with noteId {} and assetId {}", request.getNoteId(), asset.getId());
    }

    // [GET]: /sets/{setId}/notes/all
    @Override
    public Page<GetAllNotesResponseDTO> getAllNotes(Long setId, String q, Privacy privacy, Pageable pageable) {
        Long userId = authenticationContext.getCurrentUserId();

        Page<Note> pagedNote;

        if (q == null || q.isBlank()) {
            if (privacy == null) {
                pagedNote = noteRepository.findByUserIdAndSetId(userId, setId, pageable);
            } else {
                pagedNote = noteRepository.findByUserIdAndSetIdAndPrivacy(userId, setId, privacy.name(), pageable);
            }
        } else {
            if (privacy == null) {
                pagedNote = noteRepository.searchByUserIdAndSetId(userId, setId, q, pageable);
            } else {
                pagedNote = noteRepository.searchByUserIdAndSetIdAndPrivacy(userId, setId, q, privacy.name(), pageable);
            }
        }

        return pagedNote.map(note -> GetAllNotesResponseDTO.builder()
                .id(note.getId())
                .title(note.getTitle())
                .description(note.getDescription())
                .privacy(note.getPrivacy())
                .created_at(note.getCreatedAt() != null ? note.getCreatedAt().toString() : null)
                .updated_at(note.getUpdatedAt() != null ? note.getUpdatedAt().toString() : null)
                .build());
    }

    // [GET]: /sets/{setId}/notes/{noteId}
    @Override
    public GetDetailNoteResponseDTO getDetailNote(Long setId, Long noteId) {
        Long userId = authenticationContext.getCurrentUserId();
        // Note note = getNoteByIdAndUserIdAndSetId(noteId, userId, setId);
        Note note = getNoteById(noteId);

        NoteRole noteRole = notePermissionService.getUserRoleInNote(noteId, userId);


        return GetDetailNoteResponseDTO.builder()
                .id(note.getId())
                .setId(note.getSet() != null ? note.getSet().getId() : null)
                .title(note.getTitle())
                .description(note.getDescription())
                .privacy(note.getPrivacy())
                .content(note.getContent())
                .userRole(noteRole)
                .noteDocs(
                        note.getNoteDocs().stream()
                                .map(doc -> {
                                    Asset asset = doc.getAsset();

                                    return GetDocsInNoteResponseDTO.builder()
                                            .assetId(asset.getId())
                                            .fileName(asset.getFileName())
                                            .fileUrl(asset.getUrl())
                                            .publicId(asset.getPublicId())
                                            .build();
                                })
                                .toList()
                )
                .noteImgs(
                        note.getNoteImgs().stream()
                                .map(img -> {
                                    Asset asset = img.getAsset();
                                    return GetDocsInNoteResponseDTO.builder()
                                            .assetId(asset.getId())
                                            .fileName(asset.getFileName())
                                            .fileUrl(asset.getUrl())
                                            .publicId(asset.getPublicId())
                                            .build();
                                })
                                .toList()
                )
                .build();
    }

    // [PATCH]: /sets/{setId}/notes/update/{noteId}
    @Override
    public void updateNote(Long setId, Long noteId, UpdateNoteRequestDTO request) {
        Long userId = authenticationContext.getCurrentUserId();
        Note note = getNoteByIdAndUserIdAndSetId(noteId, userId, setId);

        note.setTitle(request.getTitle());
        note.setPrivacy(request.getPrivacy());
        note.setDescription(request.getDescription());
        noteRepository.save(note);
    }

    // [DELETE]: /api/note/delete/{noteId}
    @Override
    public void deleteNote(Long setId, Long noteId) {
        Long userId = authenticationContext.getCurrentUserId();
        Note note = getNoteByIdAndUserIdAndSetId(noteId, userId, setId);

        List<NoteDocs> noteDocs = new ArrayList<>(note.getNoteDocs());
        for (NoteDocs noteDoc : noteDocs) {
            DeleteNoteDocRequestDTO request = DeleteNoteDocRequestDTO.builder()
                    .noteId(noteId)
                    .assetId(noteDoc.getId().getAssetId())
                    .build();
            deleteDocInNote(request);
        }

        List<NoteImgs> noteImgs = new ArrayList<>(note.getNoteImgs());
        for (NoteImgs noteImg : noteImgs) {
            deleteImgInNote(noteId, noteImg.getId().getAssetId());
        }

        noteRepository.delete(note);
    }

    // ##################################################
    // #################  UTILS METHOD  #################
    // ##################################################

    private Set getSetById(Long setId) {
        return setRepository.findById(setId).orElseThrow(() -> new ResourceNotFoundException("Set not found"));
    }

    private Set getSetByIdAndUserId(Long setId, Long userId) {
        return setRepository.findByIdAndUserId(setId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Set not found or no permission"));
    }

    private Note getNoteById(Long noteId) {
        return noteRepository.findById(noteId).orElseThrow(() -> new ResourceNotFoundException("Note not found"));
    }

    private Note getNoteByIdAndUserIdAndSetId(Long noteId, Long userId, Long setId) {
        return noteRepository.findByIdAndUserIdAndSetId(noteId, userId, setId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found or no permission"));
    }

    private Asset getAssetById(Long assetId) {
        return assetRepository.findById(assetId).orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void deleteImgInNote(Long noteId, Long assetId) {
        noteFileRegionCommentService.deleteAllForNoteAndAsset(noteId, assetId);
        // Mark status "DELETED" in asset table
        Asset asset = getAssetById(assetId);
        assetService.markDeletedAsset(asset);

        // Delete from note_imgs table
        NoteImgsId noteImgsId = new NoteImgsId(assetId, noteId);
        noteImgsRepository.deleteById(noteImgsId);

        log.info("Delete img in note with noteId {} and assetId {}", noteId, assetId);
    }

    @Override
    public List<InviteResultResponse> inviteMembers(Long setId, Long noteId, InviteMemberRequest request) {
        Long userId = authenticationContext.getCurrentUserId();
        List<InviteResultResponse> results = notePermissionService.inviteMembers(
            setId, noteId, request.getTargets(), request.getRole(), userId);

        return results;
    }

    @Override
    public List<PendingInviteResponse> getPendingInvites() {
        Long userId = authenticationContext.getCurrentUserId();
        List<PendingInviteResponse> responses = notePermissionService.getPendingInvites(userId);

        return responses;
    }

    @Override
    public void acceptInvite(Long noteId) {
        notePermissionService.acceptInvite(noteId, authenticationContext.getCurrentUserId());
    }

    @Override
    public void declineInvite(Long noteId) {
        notePermissionService.declineInvite(noteId, authenticationContext.getCurrentUserId());
    }

    @Override
    public void removeMember(Long noteId, Long targetUserId) {
        notePermissionService.removeMember(noteId, targetUserId, authenticationContext.getCurrentUserId());
    }

    @Override
    public AcceptByTokenResponse acceptByToken(String token) {
        AcceptByTokenResponse response = notePermissionService.acceptByToken(token);

        return response;
    }
}

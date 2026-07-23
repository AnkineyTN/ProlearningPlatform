package com.cabybara.prolearningplatform.service.note.impl;

import com.cabybara.prolearningplatform.dto.request.note.*;
import com.cabybara.prolearningplatform.dto.request.share.InviteMemberRequest;
import com.cabybara.prolearningplatform.dto.response.*;
import com.cabybara.prolearningplatform.dto.response.note.AcceptByTokenResponse;
import com.cabybara.prolearningplatform.dto.response.note.CreateNoteResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.GenerateNoteWithAIResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.GetAllNotesResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.SharedNoteResponseDto;
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
import com.cabybara.prolearningplatform.service.ai.AINoteService;
import com.cabybara.prolearningplatform.service.note.NoteFileRegionCommentService;
import com.cabybara.prolearningplatform.service.note.NoteService;
import com.cabybara.prolearningplatform.service.permission.impl.NotePermissionService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private final AINoteService aiNoteService;
    private final UserFavoriteResourceRepository userFavoriteResourceRepository;

    // ##################################################
    // #################  MAIN METHOD  ##################
    // ##################################################

    // [POST]: /sets/{setId}/notes
    @Override
    @Transactional
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
        setRepository.updateLastModifiedDate(setId, OffsetDateTime.now());

        return CreateNoteResponseDTO.builder()
                .noteId(saved.getId())
                .build();
    }

    @Override
    @Transactional
    public GenerateNoteWithAIResponseDTO createNoteWithAI(Long setId, GenerateNoteWithAIRequestDTO request) {
        Long userId = authenticationContext.getCurrentUserId();
        User user = getUserById(userId);
        Set set = getSetByIdAndUserId(setId, userId);

        GenerateNoteWithAIResponseDTO aiResponse = aiNoteService.generateNoteContent(request);

        Note note = Note.builder()
                .title(aiResponse.getTitle())
                .content(aiResponse.getContent())
                .description(request.getDescription())
                .privacy(request.getPrivacy())
                .set(set)
                .user(user)
                .build();
        Note saved = noteRepository.save(note);
        log.info("✅ Created note with AI '{}' in set id {} by user {}", saved.getTitle(), set.getId(), userId);

        notePermissionService.addOwner(saved.getId(), userId);
        setRepository.updateLastModifiedDate(setId, OffsetDateTime.now());

        return GenerateNoteWithAIResponseDTO.builder()
                .noteId(saved.getId())
                .title(aiResponse.getTitle())
                .content(aiResponse.getContent())
                .build();
    }

    // [PATCH]: /sets/{setId}/notes/save
    @Override
    @Transactional
    @CacheEvict(value = "note_detail", allEntries = true)
    public void saveNote(Long setId, Long noteId, SaveNoteRequestDTO request) {
        Long userId = authenticationContext.getCurrentUserId();
        Note note = getEditableNoteByIdAndSetId(noteId, setId, userId);

        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        noteRepository.save(note);
        setRepository.updateLastModifiedDate(setId, OffsetDateTime.now());
        log.info("✅ Save note with noteId '{}'", noteId);
    }

    // [POST]: /sets/{setId}/notes/save-doc
    @Override
    @Transactional
    @CacheEvict(value = "note_detail", allEntries = true)
    public void saveDocInNote(Long setId, SaveDocInNoteRequestDto request) {
        Long userId = authenticationContext.getCurrentUserId();
        Note note = getEditableNoteByIdAndSetId(request.getNoteId(), setId, userId);
        Asset asset = getAssetById(request.getAssetId());

        NoteDocs noteDocs = new NoteDocs(note, asset);

        noteDocsRepository.save(noteDocs);
        setRepository.updateLastModifiedDate(setId, OffsetDateTime.now());
        log.info("✅ Save document in note with noteId {} and assetId {}", note.getTitle(), asset.getId());
    }

    // [DELETE]: /sets/{setId}/notes/delete-doc
    @Override
    @Transactional
    @CacheEvict(value = "note_detail", allEntries = true)
    public void deleteDocInNote(DeleteNoteDocRequestDTO request) {
        Long userId = authenticationContext.getCurrentUserId();
        Note note = getNoteById(request.getNoteId());

        if (!notePermissionService.canEdit(userId, note.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: cannot delete doc in this note");
        }

        noteFileRegionCommentService.deleteAllForNoteAndAsset(request.getNoteId(), request.getAssetId());
        // Mark status "DELETED" in asset table
        Asset asset = getAssetById(request.getAssetId());
        assetService.markDeletedAsset(asset);

        // Delete from note_docs table
        NoteDocsId noteDocsId = new NoteDocsId(request.getNoteId(), request.getAssetId());
        noteDocsRepository.deleteById(noteDocsId);

        if (note.getSet() != null) {
            setRepository.updateLastModifiedDate(note.getSet().getId(), OffsetDateTime.now());
        }
        log.info("✅ Delete doc in note with noteId {} and assetId {}", request.getNoteId(), request.getAssetId());
    }

    // [POST]: /sets/{setId}/notes/save-img
    @Override
    @Transactional
    @CacheEvict(value = "note_detail", allEntries = true)
    public void saveImgInNote(Long setId, SaveImgInNoteRequestDto request) {
        Long userId = authenticationContext.getCurrentUserId();
        Note note = getEditableNoteByIdAndSetId(request.getNoteId(), setId, userId);
        Asset asset = getAssetById(request.getAssetId());

        NoteImgs noteImgs = new NoteImgs(note, asset);

        noteImgsRepository.save(noteImgs);
        setRepository.updateLastModifiedDate(setId, OffsetDateTime.now());
        log.info("✅ Save image in note with noteId {} and assetId {}", note.getTitle(), asset.getId());
    }

    // [DELETE]: /sets/{setId}/notes/delete-img
    @Override
    @Transactional
    @CacheEvict(value = "note_detail", allEntries = true)
    public void deleteImgInNote(DeleteNoteImgRequestDTO request) {
        Long userId = authenticationContext.getCurrentUserId();
        Note note = getNoteById(request.getNoteId());

        if (!notePermissionService.canEdit(userId, note.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: cannot delete img in this note");
        }

        Asset asset = assetRepository.findByUrl(request.getFileUrl());
        noteFileRegionCommentService.deleteAllForNoteAndAsset(request.getNoteId(), asset.getId());
        // Mark status "DELETED" in asset table
        assetService.markDeletedAsset(asset);

        // Delete from note_imgs table
        NoteImgsId noteImgsId = new NoteImgsId(request.getNoteId(), asset.getId());
        noteImgsRepository.deleteById(noteImgsId);

        if (note.getSet() != null) {
            setRepository.updateLastModifiedDate(note.getSet().getId(), OffsetDateTime.now());
        }
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
                .noteDocs(note.getNoteDocs() != null ? note.getNoteDocs().stream()
                        .map(doc -> GetDocsInNoteResponseDTO.builder()
                                .assetId(doc.getAsset().getId())
                                .fileName(doc.getAsset().getFileName())
                                .fileUrl(doc.getAsset().getUrl())
                                .publicId(doc.getAsset().getPublicId())
                                .build())
                        .toList() : null)
                .isFavorited(userFavoriteResourceRepository.existsByUserIdAndResourceIdAndResourceType(userId, note.getId(), com.cabybara.prolearningplatform.enums.ContentType.NOTE))
                .build());
    }

    // [GET]: /sets/{setId}/notes/{noteId}
    @Override
    @Cacheable(value = "note_detail", key = "@authenticationContext.getCurrentUserId() + ':' + #setId + ':' + #noteId")
    public GetDetailNoteResponseDTO getDetailNote(Long setId, Long noteId) {
        Long userId = authenticationContext.getCurrentUserId();
        // Note note = getNoteByIdAndUserIdAndSetId(noteId, userId, setId);
        Note note = getNoteByIdAndSetId(noteId, setId);

        NoteRole noteRole = notePermissionService.getUserRoleInNote(noteId, userId);
        boolean isFavorited = userFavoriteResourceRepository.existsByUserIdAndResourceIdAndResourceType(userId, noteId, com.cabybara.prolearningplatform.enums.ContentType.NOTE);

        return GetDetailNoteResponseDTO.builder()
                .id(note.getId())
                .setId(note.getSet() != null ? note.getSet().getId() : null)
                .title(note.getTitle())
                .description(note.getDescription())
                .privacy(note.getPrivacy())
                .content(note.getContent())
                .userRole(noteRole)
                .isFavorited(isFavorited)
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
                .ownerId(note.getUser().getId())
                .ownerName(note.getUser().getFirstName() != null ? note.getUser().getFirstName() + " " + note.getUser().getLastName() : note.getUser().getLastName())
                .ownerAvatar(note.getUser().getAvatarUrl())
                .build();
    }

    // [PATCH]: /sets/{setId}/notes/update/{noteId}
    @Override
    @Transactional
    @CacheEvict(value = "note_detail", allEntries = true)
    public void updateNote(Long setId, Long noteId, UpdateNoteRequestDTO request) {
        Long userId = authenticationContext.getCurrentUserId();
        Note note = getEditableNoteByIdAndSetId(noteId, setId, userId);

        note.setTitle(request.getTitle());
        note.setPrivacy(request.getPrivacy());
        note.setDescription(request.getDescription());
        noteRepository.save(note);
    }

    // [DELETE]: /api/note/delete/{noteId}
    @Override
    @Transactional
    @CacheEvict(value = "note_detail", allEntries = true)
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
        Set set = setRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Set with id " + setId + " not found"));

        if (!Objects.equals(set.getUser().getId(), userId)) {
            throw new AccessDeniedException("You are not allowed to access this set.");
        }

        return set;
    }

    private Note getNoteById(Long noteId) {
        return noteRepository.findById(noteId).orElseThrow(() -> new ResourceNotFoundException("Note not found"));
    }

    private Note getNoteByIdAndSetId(Long noteId, Long setId) {
        return noteRepository.findByIdAndSetId(noteId, setId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found or no permission"));
    }

    private Note getEditableNoteByIdAndSetId(Long noteId, Long setId, Long userId) {
        Note note = getNoteByIdAndSetId(noteId, setId);

        if (!notePermissionService.canEdit(userId, noteId)) {
            throw new AccessDeniedException(noteId.toString());
        }

        return note;
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
    @CacheEvict(value = "note_detail", allEntries = true)
    public void acceptInvite(Long noteId) {
        notePermissionService.acceptInvite(noteId, authenticationContext.getCurrentUserId());
    }

    @Override
    @CacheEvict(value = "note_detail", allEntries = true)
    public void declineInvite(Long noteId) {
        notePermissionService.declineInvite(noteId, authenticationContext.getCurrentUserId());
    }

    @Override
    @CacheEvict(value = "note_detail", allEntries = true)
    public void removeMember(Long noteId, Long targetUserId) {
        notePermissionService.removeMember(noteId, targetUserId, authenticationContext.getCurrentUserId());
    }

    @Override
    public AcceptByTokenResponse acceptByToken(String token) {
        AcceptByTokenResponse response = notePermissionService.acceptByToken(token);

        return response;
    }

    @Override
    public Page<SharedNoteResponseDto> getSharedNotes(String q, Privacy privacy, Pageable pageable) {
        Long userId = authenticationContext.getCurrentUserId();
        String privacyFilter = privacy != null ? privacy.name() : null;

        return noteRepository.findSharedNotes(userId, q, privacyFilter, pageable)
                .map(note -> {
                    NoteRole role = notePermissionService.getUserRoleInNote(note.getId(), userId);
                    return SharedNoteResponseDto.builder()
                            .id(note.getId())
                            .title(note.getTitle())
                            .description(note.getDescription())
                            .privacy(note.getPrivacy())
                            .created_at(note.getCreatedAt() != null ? note.getCreatedAt().toString() : null)
                            .updated_at(note.getUpdatedAt() != null ? note.getUpdatedAt().toString() : null)
                            .setId(note.getSet() != null ? note.getSet().getId() : null)
                            .userRole(role)
                            .isFavorited(userFavoriteResourceRepository.existsByUserIdAndResourceIdAndResourceType(userId, note.getId(), com.cabybara.prolearningplatform.enums.ContentType.NOTE))
                            .ownerId(note.getUser().getId())
                            .ownerName(note.getUser().getFirstName() != null ? note.getUser().getFirstName() + " " + note.getUser().getLastName() : note.getUser().getLastName())
                            .ownerAvatar(note.getUser().getAvatarUrl())
                            .build();
                });
    }
}

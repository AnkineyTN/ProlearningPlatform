package com.cabybara.prolearningplatform.service.note.impl;

import com.cabybara.prolearningplatform.dto.request.note.*;
import com.cabybara.prolearningplatform.dto.response.*;
import com.cabybara.prolearningplatform.dto.response.note.CreateNoteResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.GetAllNotesResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.GetDetailNoteResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.GetDocsInNoteResponseDTO;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.*;
import com.cabybara.prolearningplatform.model.composite_key.NoteDocsId;
import com.cabybara.prolearningplatform.model.composite_key.NoteImgsId;
import com.cabybara.prolearningplatform.repository.*;
import com.cabybara.prolearningplatform.service.asset.AssetService;
import com.cabybara.prolearningplatform.service.cloudinary.CloudinaryService;
import com.cabybara.prolearningplatform.service.note.NoteService;
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
    private final SetRepository setRepository;
    private final NoteRepository noteRepository;
    private final NoteDocsRepository noteDocsRepository;
    private final NoteImgsRepository noteImgsRepository;
    private final AssetRepository assetRepository;
    private final CloudinaryService cloudinaryService;

    private final AssetService assetService;

    // [POST]: /api/note/create
    @Override
    public CreateNoteResponseDTO createNote(CreateNoteRequestDTO request) {
        Set set = getSetById(request.getSetId());

        Note note = Note.builder()
                .title(request.getTitle())
                .privacy(request.getPrivacy())
                .description(request.getDescription())
                .set(set)
                .build();
        Note saved = noteRepository.save(note);
        log.info("🐳️ Created note '{}' in set id {}", note.getTitle(), set.getId());

        return CreateNoteResponseDTO.builder()
                .noteId(saved.getId())
                .build();
    }

    // [PATCH]: /api/note/save
    @Override
    public void saveNote(Long noteId, SaveNoteRequestDTO request) {
        Note note = getNoteById(noteId);
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        noteRepository.save(note);
        log.info("🐳 Save note '{}'", noteId);
    }

    // TODO: Added API save doc in note
    // [POST]: /api/note/save-doc
    @Override
    public void saveDocInNote(SaveDocInNoteRequestDto request) {
        Note note = getNoteById(request.getNoteId());
        Asset asset = getAssetById(request.getAssetId());

        NoteDocs noteDocs = new NoteDocs(note, asset);

        noteDocsRepository.save(noteDocs);
        log.info("🐳️ Save document in note with noteId {} and assetId {}", note.getTitle(), asset.getId());
    }

    // [DELETE]: /api/note/delete-doc
    @Override
    public void deleteDocInNote(DeleteNoteDocRequestDTO request) {
        // Mark status "DELETED" in asset table
        Asset asset = getAssetById(request.getAssetId());
        assetService.markDeletedAsset(asset);

        // Delete from note_docs table
        NoteDocsId noteDocsId = new NoteDocsId(request.getNoteId(), request.getAssetId());
        noteDocsRepository.deleteById(noteDocsId);

        log.info("🐳️ Delete doc in note with noteId {} and assetId {}", request.getNoteId(), request.getAssetId());
    }

    // TODO: Added API save img in note
    // [POST]: /api/note/save-img
    @Override
    public void saveImgInNote(SaveImgInNoteRequestDto request) {
        Note note = getNoteById(request.getNoteId());
        Asset asset = getAssetById(request.getAssetId());

        NoteImgs noteImgs = new NoteImgs(note, asset);

        noteImgsRepository.save(noteImgs);
        log.info("🐳️ Save image in note with noteId {} and assetId {}", note.getTitle(), asset.getId());
    }

    // [DELETE]: /api/note/delete-img
    @Override
    public void deleteImgInNote(DeleteNoteImgRequestDTO request) {
        // Mark status "DELETED" in asset table
        Asset asset = assetRepository.findByUrl(request.getFileUrl());
        assetService.markDeletedAsset(asset);

        // Delete from note_imgs table
        NoteImgsId noteImgsId = new NoteImgsId(request.getNoteId(), asset.getId());
        noteImgsRepository.deleteById(noteImgsId);

        log.info("🐳️ Delete img in note with noteId {} and assetId {}", request.getNoteId(), asset.getId());
    }

    // [GET]: /api/note/all/{setId}
    @Override
    public PageResponseDetail<?> getAllNotesOfSet(int pageNo, int pageSize, Long setId) {
        int page = 0;
        if (pageNo > 0) {
            page = pageNo - 1;
        }

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        Page<Note> notesPage = noteRepository.findNotesBySetId(setId, pageable);

        List<GetAllNotesResponseDTO> noteDTOs = notesPage.getContent().stream()
                .map(note -> GetAllNotesResponseDTO.builder()
                        .id(note.getId())
                        .title(note.getTitle())
                        .description(note.getDescription())
                        .privacy(note.getPrivacy())
                        .created_at(note.getCreatedAt() != null ? note.getCreatedAt().toString() : null)
                        .updated_at(note.getUpdatedAt() != null ? note.getUpdatedAt().toString() : null)
                        .build())
                .toList();

        log.info("🐳️ Get all note of set with setId {}", setId);
        return PageResponseDetail.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(notesPage.getTotalPages())
                .totalElements(notesPage.getTotalElements())
                .items(noteDTOs)
                .build();
    }

    // TODO: Fixed return field of doc in note
    // [GET]: /api/note/{noteId}
    @Override
    public GetDetailNoteResponseDTO getDetailNote(Long noteId) {
        Note note = getNoteById(noteId);

        log.info("🐳️ Get detail of note with noteId", noteId);
        return GetDetailNoteResponseDTO.builder()
                .id(note.getId())
                .title(note.getTitle())
                .description(note.getDescription())
                .privacy(note.getPrivacy())
                .content(note.getContent())
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
                .build();
    }

    // [PATCH]: /api/note/update/{noteId}
    @Override
    public void updateNote(Long noteId, UpdateNoteRequestDTO request) {
        Note note = getNoteById(noteId);
        note.setTitle(request.getTitle());
        note.setPrivacy(request.getPrivacy());
        note.setDescription(request.getDescription());
        noteRepository.save(note);
    }

    // [DELETE]: /api/note/delete/{noteId}
    @Override
    public void deleteNote(Long noteId) {
        Note note = getNoteById(noteId);

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

    private Set getSetById(Long setId) {
        return setRepository.findById(setId).orElseThrow(() -> new ResourceNotFoundException("Set not found"));
    }

    private Note getNoteById(Long noteId) {
        return noteRepository.findById(noteId).orElseThrow(() -> new ResourceNotFoundException("Note not found"));
    }

    private Asset getAssetById(Long assetId) {
        return assetRepository.findById(assetId).orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
    }

    private void deleteImgInNote(Long noteId, Long assetId) {
        // Mark status "DELETED" in asset table
        Asset asset = getAssetById(assetId);
        assetService.markDeletedAsset(asset);

        // Delete from note_imgs table
        NoteImgsId noteImgsId = new NoteImgsId(assetId, noteId);
        noteImgsRepository.deleteById(noteImgsId);

        log.info("🐳️ Delete img in note with noteId {} and assetId {}", noteId, assetId);
    }
}

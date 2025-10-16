package com.cabybara.prolearningplatform.service.note.impl;

import com.cabybara.prolearningplatform.dto.request.*;
import com.cabybara.prolearningplatform.dto.response.*;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.Note;
import com.cabybara.prolearningplatform.model.NoteDocs;
import com.cabybara.prolearningplatform.model.NoteImgs;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.repository.NoteDocsRepository;
import com.cabybara.prolearningplatform.repository.NoteImgsRepository;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.repository.SetRepository;
import com.cabybara.prolearningplatform.service.cloudinary.CloudinaryService;
import com.cabybara.prolearningplatform.service.note.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {
    private final SetRepository setRepository;
    private final NoteRepository noteRepository;
    private final NoteDocsRepository noteDocsRepository;
    private final NoteImgsRepository noteImgsRepository;
    private final CloudinaryService cloudinaryService;

    // [POST]: /api/note/create
    @Override
    public void createNote(CreateNoteRequestDTO request) {
        Set set = getSetById(request.getSetId());

        Note note = Note.builder()
                .title(request.getTitle())
                .privacy(request.getPrivacy())
                .description(request.getDescription())
                .set(set)
                .build();
        noteRepository.save(note);
        log.info("✅ Created note '{}' in set id {}", note.getTitle(), set.getId());
    }

    // [PATCh]: /api/note/save
    @Override
    public void saveNote(Long noteId, SaveNoteRequestDTO request) {
        Note note = getNoteById(noteId);
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        noteRepository.save(note);
        log.info("✅ Save note '{}'", noteId);
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

        return PageResponseDetail.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(notesPage.getTotalPages())
                .totalElements(notesPage.getTotalElements())
                .items(noteDTOs)
                .build();
    }

    // [GET]: /api/note/{noteId}
    @Override
    public GetDetailNoteResponseDTO getDetailNote(Long noteId) {
        Note note = noteRepository.findNoteWithDocsById(noteId).orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + noteId));

        return GetDetailNoteResponseDTO.builder()
                .id(note.getId())
                .title(note.getTitle())
                .description(note.getDescription())
                .privacy(note.getPrivacy())
                .content(note.getContent())
                .noteDocs(
                        note.getNoteDocs().stream()
                                .map(doc -> GetDocsInNoteResponseDTO.builder()
                                        .id(doc.getId())
                                        .fileName(doc.getFileName())
                                        .fileUrl(doc.getFileUrl())
                                        .extension(doc.getExtension())
                                        .publicId(doc.getPublicId())
                                        .build())
                                .toList()
                )
                .build();
    }

    // [DELETE]: /api/note/delete-doc/{noteDocsId}
    @Override
    public void deleteDocInNote(Long noteDocsId, DeleteNoteDocRequestDTO request) throws IOException {
        noteDocsRepository.deleteById(noteDocsId);
        cloudinaryService.deleteFile(request.getPublicId(), request.getExtension());
    }

    // [DELETE]: /api/note/delete-img
    @Override
    public void deleteImgInNote(DeleteNoteImgRequestDTO request) throws IOException {
        NoteImgs noteImg = noteImgsRepository.findByFileUrl(request.getFileUrl());
        noteImgsRepository.deleteById(noteImg.getId());
        cloudinaryService.deleteFile(noteImg.getPublicId(), noteImg.getExtension());
    }

    @Override
    public void updateNote(Long noteId, UpdateNoteRequestDTO request) {
        Note note = getNoteById(noteId);
        note.setTitle(request.getTitle());
        note.setPrivacy(request.getPrivacy());
        note.setDescription(request.getDescription());
        noteRepository.save(note);
    }

    @Override
    public void deleteNote(Long noteId) throws IOException {
        Note note =  getNoteById(noteId);

        List<NoteDocs> noteDocs = note.getNoteDocs();
        for(NoteDocs noteDoc : noteDocs) {
            DeleteNoteDocRequestDTO deleteDocReq = new  DeleteNoteDocRequestDTO();
            deleteDocReq.setPublicId(noteDoc.getPublicId());
            deleteDocReq.setExtension(noteDoc.getExtension());
            deleteDocInNote(noteDoc.getId(), deleteDocReq);
        }

        List<NoteImgs> noteImgs = note.getNoteImgs();
        for(NoteImgs noteImg : noteImgs) {
            DeleteNoteImgRequestDTO deleteNoteImgReq = new  DeleteNoteImgRequestDTO();
            deleteNoteImgReq.setFileUrl(noteImg.getFileUrl());
            deleteImgInNote(deleteNoteImgReq);
        }

        noteRepository.deleteById(noteId);
    }

    private Set getSetById(Long setId) {
        return setRepository.findById(setId).orElseThrow(() -> new ResourceNotFoundException("Set not found"));
    }

    private Note getNoteById(Long noteId) {
        return noteRepository.findById(noteId).orElseThrow(() -> new ResourceNotFoundException("Note not found"));
    }
}

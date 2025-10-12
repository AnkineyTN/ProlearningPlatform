package com.cabybara.prolearningplatform.service.impl;

import com.cabybara.prolearningplatform.dto.request.CreateNoteRequestDTO;
import com.cabybara.prolearningplatform.dto.request.SaveNoteRequestDTO;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.Note;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.repository.SetRepository;
import com.cabybara.prolearningplatform.service.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {
    private final SetRepository setRepository;
    private final NoteRepository noteRepository;

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

    @Override
    public void saveNote(Long noteId, SaveNoteRequestDTO request) {
        Note note = getNoteById(noteId);
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        noteRepository.save(note);
        log.info("✅ Updated note '{}'", noteId);
    }

    private Set getSetById(Long setId) {
        return setRepository.findById(setId).orElseThrow(() -> new ResourceNotFoundException("Set not found"));
    }

    private Note getNoteById(Long noteId) {
        return noteRepository.findById(noteId).orElseThrow(() -> new ResourceNotFoundException("Note not found"));
    }
}

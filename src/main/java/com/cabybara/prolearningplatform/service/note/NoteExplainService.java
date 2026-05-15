package com.cabybara.prolearningplatform.service.note;

import com.cabybara.prolearningplatform.dto.request.note.SaveNoteExplainRequestDTO;
import com.cabybara.prolearningplatform.dto.request.note.UpdateNoteExplainRequestDTO;
import com.cabybara.prolearningplatform.dto.response.note.NoteExplainResponseDTO;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.note.Note;
import com.cabybara.prolearningplatform.model.note.NoteExplain;
import com.cabybara.prolearningplatform.repository.NoteExplainRepository;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteExplainService {
    
    private final NoteExplainRepository noteExplainRepository;
    private final NoteRepository noteRepository;
    
    /**
     * Get all explanations for a note
     */
    public List<NoteExplainResponseDTO> getExplanationsByNote(Long noteId) {
        return noteExplainRepository.findByNoteId(noteId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get explanation by ID
     */
    public NoteExplainResponseDTO getExplanationById(Long explainId) {
        NoteExplain explain = noteExplainRepository.findById(explainId)
            .orElseThrow(() -> new ResourceNotFoundException("Explanation not found with id: " + explainId));
        return mapToResponse(explain);
    }
    
    /**
     * Save new explanation
     */
    public NoteExplainResponseDTO saveExplanation(SaveNoteExplainRequestDTO request) {
        Note note = noteRepository.findById(request.getNoteId())
            .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + request.getNoteId()));
        
        NoteExplain explain = NoteExplain.builder()
            .note(note)
            .source(request.getSource())
            .term(request.getTerm())
            .explain(request.getExplain())
            .build();
        
        NoteExplain saved = noteExplainRepository.save(explain);
        log.info("Explanation saved successfully with id: {}", saved.getId());
        return mapToResponse(saved);
    }
    
    /**
     * Update existing explanation
     */
    public NoteExplainResponseDTO updateExplanation(Long explainId, UpdateNoteExplainRequestDTO request) {
        NoteExplain explain = noteExplainRepository.findById(explainId)
            .orElseThrow(() -> new ResourceNotFoundException("Explanation not found with id: " + explainId));
        
        explain.setSource(request.getSource());
        explain.setTerm(request.getTerm());
        explain.setExplain(request.getExplain());
        
        NoteExplain updated = noteExplainRepository.save(explain);
        log.info("Explanation updated successfully with id: {}", explainId);
        return mapToResponse(updated);
    }
    
    /**
     * Delete explanation by ID
     */
    public void deleteExplanation(Long explainId) {
        if (!noteExplainRepository.existsById(explainId)) {
            throw new ResourceNotFoundException("Explanation not found with id: " + explainId);
        }
        noteExplainRepository.deleteById(explainId);
        log.info("Explanation deleted successfully with id: {}", explainId);
    }
    
    /**
     * Delete all explanations for a note
     */
    public void deleteExplanationsByNote(Long noteId) {
        noteExplainRepository.deleteByNoteId(noteId);
        log.info("All explanations deleted for note id: {}", noteId);
    }
    
    /**
     * Map NoteExplain entity to response DTO
     */
    private NoteExplainResponseDTO mapToResponse(NoteExplain explain) {
        return NoteExplainResponseDTO.builder()
            .id(explain.getId())
            .noteId(explain.getNote().getId())
            .source(explain.getSource())
            .term(explain.getTerm())
            .explain(explain.getExplain())
            .createdAt(explain.getCreatedAt())
            .updatedAt(explain.getUpdatedAt())
            .build();
    }
}

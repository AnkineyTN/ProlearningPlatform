package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.note.NoteExplain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NoteExplainRepository extends JpaRepository<NoteExplain, Long> {
    List<NoteExplain> findByNoteId(Long noteId);
    void deleteByNoteId(Long noteId);
}

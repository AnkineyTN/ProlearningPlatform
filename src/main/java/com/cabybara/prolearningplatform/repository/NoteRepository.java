package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.Note;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    @Query("""
                SELECT n
                FROM Note n
                WHERE n.set.id = :setId
            """)
    Page<Note> findNotesBySetId(@Param("setId") Long setId, Pageable pageable);
}

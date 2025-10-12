package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

}

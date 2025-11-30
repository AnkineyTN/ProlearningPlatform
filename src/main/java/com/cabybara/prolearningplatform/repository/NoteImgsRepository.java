package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.NoteImgs;
import com.cabybara.prolearningplatform.model.composite_key.NoteImgsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteImgsRepository extends JpaRepository<NoteImgs, NoteImgsId> {
}

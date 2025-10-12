package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.NoteDocs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteDocsRepository extends JpaRepository<NoteDocs, Long> {

}

package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.note.NoteFileRegionComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteFileRegionCommentRepository extends JpaRepository<NoteFileRegionComment, Long> {

    List<NoteFileRegionComment> findByNote_IdOrderByCreatedAtAsc(Long noteId);

    List<NoteFileRegionComment> findByNote_IdAndAsset_IdOrderByCreatedAtAsc(Long noteId, Long assetId);
}

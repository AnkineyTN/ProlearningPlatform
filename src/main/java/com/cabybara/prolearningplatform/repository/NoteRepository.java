package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.note.Note;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    /** Đếm số Note thuộc sở hữu của user (dùng để kiểm tra quota). */
    @Query("SELECT COUNT(n) FROM Note n WHERE n.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("""
                SELECT n
                FROM Note n
                WHERE n.set.id = :setId
            """)
    Page<Note> findNotesBySetId(@Param("setId") Long setId, Pageable pageable);

    Optional<Note> findByIdAndUserIdAndSetId(Long noteId, Long userId, Long setId);

    @Query("""
                SELECT n FROM Note n
                LEFT JOIN FETCH n.noteDocs
                WHERE n.id = :noteId
            """)
    Optional<Note> findNoteWithDocsById(@Param("noteId") Long noteId);

    @Query(
            value = """
                SELECT *
                FROM note
                WHERE id_user = :userId AND id_set = :setId
            """,
            nativeQuery = true)
    Page<Note> findByUserIdAndSetId(Long userId, Long setId, Pageable pageable);

    @Query(
            value = """
                SELECT *
                FROM note
                WHERE id_user = :userId AND id_set = :setId AND privacy = :privacy
            """,
            nativeQuery = true)
    Page<Note> findByUserIdAndSetIdAndPrivacy(Long userId, Long setId, String privacy, Pageable pageable);

    @Query(
            value = """
                SELECT *
                FROM note
                WHERE id_user = :userId AND id_set = :setId
                  AND (search_vector @@ plainto_tsquery('simple', unaccent(:q))
                      OR (title || ' ' || description) % unaccent(:q))
            """,
            countQuery = """
                SELECT count(*)
                FROM note
                WHERE id_user = :userId AND id_set = :setId
                  AND (
                        search_vector @@ plainto_tsquery('simple', :q)
                        OR (title || ' ' || description) % unaccent(:q))
                """,
            nativeQuery = true)
    Page<Note> searchByUserIdAndSetId(Long userId, Long setId, String q, Pageable pageable);

    @Query(
            value = """
                SELECT *
                FROM note
                WHERE id_user = :userId
                  AND id_set = :setId
                  AND privacy = :privacy
                  AND (search_vector @@ plainto_tsquery('simple', unaccent(:q))
                      OR (title || ' ' || description) % unaccent(:q))
            """,
            countQuery = """
                SELECT *
                FROM note
                WHERE id_user = :userId
                  AND id_set = :setId
                  AND privacy = :privacy
                  AND (search_vector @@ plainto_tsquery('simple', unaccent(:q))
                      OR (title || ' ' || description) % unaccent(:q))
            """,
            nativeQuery = true)
    Page<Note> searchByUserIdAndSetIdAndPrivacy(Long userId, Long setId, String q, String privacy, Pageable pageable);

    @Query("SELECT n.yjsState FROM Note n WHERE n.id = :noteId")
    Optional<byte[]> findYjsStateById(@Param("noteId") Long noteId);

    @Modifying
    @Transactional
    @Query("UPDATE Note n SET n.yjsState = :yjsState WHERE n.id = :noteId")
    int updateYjsState(@Param("noteId") Long noteId, @Param("yjsState") byte[] yjsState);

    @Query("SELECT n.title FROM Note n WHERE n.id = :noteId")
    String findTitleById(@Param("noteId") Long noteId);

    @Query("SELECT n.privacy FROM Note n WHERE n.id = :noteId")
    Optional<com.cabybara.prolearningplatform.enums.Privacy> findPrivacyById(@Param("noteId") Long noteId);

    Optional<Note> findByIdAndSetId(Long noteId, Long setId);
}

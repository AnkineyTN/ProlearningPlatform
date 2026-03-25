package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.exam.Exam;
import com.cabybara.prolearningplatform.model.flashcard.Flashcard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam,Long> {

    @Query("select (count(q) > 0) from Exam q where q.title = ?1 and q.set = ?2")
    boolean existsByTitleAndSet(String title, Set set);

    List<Exam> findAllBySet(Set set, Pageable pageable);

    Page<Exam> findByUserIdAndSetId(Long userId, Long setId, Pageable pageable);

    Page<Exam> findByUserIdAndSetIdAndPrivacy(Long userId, Long setId, Privacy privacy, Pageable pageable);

    @Query(
            value = """
                SELECT *
                FROM exams
                WHERE id_user = :userId AND set_id = :setId
                  AND (search_vector @@ plainto_tsquery('simple', unaccent(:q))
                      OR (title || ' ' || description) % unaccent(:q))
                ORDER BY created_at DESC
            """,
            countQuery = """
                SELECT count(*)
                FROM exams
                WHERE id_user = :userId AND set_id = :setId
                  AND (
                        search_vector @@ plainto_tsquery('simple', :q)
                        OR (title || ' ' || description) % unaccent(:q))
                """,
            nativeQuery = true)
    Page<Exam> searchByUserIdAndSetId(Long userId, Long setId, String q, Pageable pageable);

    @Query(
            value = """
                SELECT *
                FROM exams
                WHERE id_user = :userId
                  AND set_id = :setId
                  AND privacy = :privacy
                  AND (search_vector @@ plainto_tsquery('simple', unaccent(:q))
                      OR (title || ' ' || description) % unaccent(:q))
                ORDER BY created_at DESC
            """,
            countQuery = """
                SELECT *
                FROM exams
                WHERE id_user = :userId
                  AND set_id = :setId
                  AND privacy = :privacy
                  AND (search_vector @@ plainto_tsquery('simple', unaccent(:q))
                      OR (title || ' ' || description) % unaccent(:q))
            """,
            nativeQuery = true)
    Page<Exam> searchByUserIdAndSetIdAndPrivacy(Long userId, Long setId, String q, String privacy, Pageable pageable);

    @Query(
            value = "SELECT * FROM exams q WHERE q.set_id = :setId AND q.id = :examId",
            nativeQuery = true
    )
    Optional<Exam> findBySetIdAndId(Long setId, Long examId);

    boolean existsBySetIdAndId(Long setId, Long examId);
}

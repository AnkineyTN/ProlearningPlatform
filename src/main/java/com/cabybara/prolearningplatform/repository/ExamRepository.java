package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.exam.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam,Long> {

    @Query("select (count(q) > 0) from Exam q where q.title = ?1 and q.set = ?2")
    boolean existsByTitleAndSet(String title, Set set);

    List<Exam> findAllBySet(Set set, Pageable pageable);

    @Query(
            value = """
                SELECT *
                FROM exams
                WHERE created_by = :userId AND set_id = :setId
                  AND ((:createMethod IS NULL AND create_method != 'REVIEW')
                      OR (:createMethod IS NOT NULL AND create_method = :createMethod))
            """,
            nativeQuery = true)
    Page<Exam> findByCreatedByAndSetId(Long userId, Long setId, String createMethod, Pageable pageable);

    @Query(
            value = """
                SELECT *
                FROM exams
                WHERE created_by = :userId AND set_id = :setId AND privacy = :privacy
                  AND ((:createMethod IS NULL AND create_method != 'REVIEW')
                      OR (:createMethod IS NOT NULL AND create_method = :createMethod))
            """,
            nativeQuery = true)
    Page<Exam> findByCreatedByAndSetIdAndPrivacy(Long userId, Long setId, String privacy, String createMethod, Pageable pageable);

    @Query(
            value = """
                SELECT *
                FROM exams
                WHERE created_by = :userId AND set_id = :setId
                  AND ((:createMethod IS NULL AND create_method != 'REVIEW')
                      OR (:createMethod IS NOT NULL AND create_method = :createMethod))
                  AND (search_vector @@ plainto_tsquery('simple', unaccent(:q))
                      OR (title || ' ' || description) % unaccent(:q))
            """,
            countQuery = """
                SELECT count(*)
                FROM exams
                WHERE created_by = :userId AND set_id = :setId
                  AND ((:createMethod IS NULL AND create_method != 'REVIEW')
                      OR (:createMethod IS NOT NULL AND create_method = :createMethod))
                  AND (search_vector @@ plainto_tsquery('simple', unaccent(:q))
                      OR (title || ' ' || description) % unaccent(:q))
                """,
            nativeQuery = true)
    Page<Exam> searchByCreatedByAndSetId(Long userId, Long setId, String q, String createMethod, Pageable pageable);

    @Query(
            value = """
                SELECT *
                FROM exams
                WHERE created_by = :userId
                  AND set_id = :setId
                  AND privacy = :privacy
                  AND ((:createMethod IS NULL AND create_method != 'REVIEW')
                      OR (:createMethod IS NOT NULL AND create_method = :createMethod))
                  AND (search_vector @@ plainto_tsquery('simple', unaccent(:q))
                      OR (title || ' ' || description) % unaccent(:q))
            """,
            countQuery = """
                SELECT count(*)
                FROM exams
                WHERE created_by = :userId
                  AND set_id = :setId
                  AND privacy = :privacy
                  AND ((:createMethod IS NULL AND create_method != 'REVIEW')
                      OR (:createMethod IS NOT NULL AND create_method = :createMethod))
                  AND (search_vector @@ plainto_tsquery('simple', unaccent(:q))
                      OR (title || ' ' || description) % unaccent(:q))
            """,
            nativeQuery = true)
    Page<Exam> searchByCreatedByAndSetIdAndPrivacy(Long userId, Long setId, String q, String privacy, String createMethod, Pageable pageable);

    @Query(
            value = "SELECT * FROM exams q WHERE q.set_id = :setId AND q.id = :examId",
            nativeQuery = true
    )
    Optional<Exam> findBySetIdAndId(Long setId, Long examId);

    boolean existsBySetIdAndId(Long setId, Long examId);

    @Query("SELECT e.privacy FROM Exam e WHERE e.id = :examId")
    Optional<Privacy> findPrivacyById(@Param("examId") Long examId);
}

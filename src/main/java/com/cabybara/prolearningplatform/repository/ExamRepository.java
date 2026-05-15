package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.dto.helper.Social.SocialExamProjection;
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

    @Query("SELECT e.id FROM Exam e WHERE e.set.id = :setId")
    List<Long> findIdsBySetId(@Param("setId") Long setId);

    @Query(value = """
        SELECT e.* FROM exams e
        INNER JOIN exam_members em ON e.id = em.exam_id
        WHERE em.user_id = :userId AND em.status = 'ACTIVE'
        AND e.created_by != :userId
        AND (:privacy IS NULL OR e.privacy = :privacy)
        AND ((:createMethod IS NULL AND e.create_method != 'REVIEW')
            OR (:createMethod IS NOT NULL AND e.create_method = :createMethod))
        AND (:q IS NULL OR :q = '' OR (
            e.search_vector @@ plainto_tsquery('simple', unaccent(:q))
            OR (e.title || ' ' || e.description) % unaccent(:q)
        ))
    """, 
    countQuery = """
        SELECT count(*) FROM exams e
        INNER JOIN exam_members em ON e.id = em.exam_id
        WHERE em.user_id = :userId AND em.status = 'ACTIVE'
        AND e.created_by != :userId
        AND (:privacy IS NULL OR e.privacy = :privacy)
        AND ((:createMethod IS NULL AND e.create_method != 'REVIEW')
            OR (:createMethod IS NOT NULL AND e.create_method = :createMethod))
        AND (:q IS NULL OR :q = '' OR (
            e.search_vector @@ plainto_tsquery('simple', unaccent(:q))
            OR (e.title || ' ' || e.description) % unaccent(:q)
        ))
    """,
    nativeQuery = true)
    Page<Exam> findSharedExams(
        @Param("userId") Long userId, 
        @Param("q") String q, 
        @Param("privacy") String privacy, 
        @Param("createMethod") String createMethod, 
        Pageable pageable);

    @Query(value = """
        SELECT
            e.id          AS id,
            e.title       AS title,
            e.description AS description,
            e.duration    AS duration,
            e.created_at  AS createdAt,
            e.updated_at  AS updatedAt,
            u.id          AS ownerId,
            u.first_name  AS ownerFirstName,
            u.last_name   AS ownerLastName,
            COUNT(eq.id)  AS numQuestions
        FROM exams e
        INNER JOIN users u ON e.created_by = u.id
        LEFT  JOIN exam_questions eq ON eq.exam_id = e.id
        WHERE e.privacy = 'PUBLIC'
          AND e.create_method IN ('MANUAL', 'AI')
          AND (:q IS NULL OR :q = '' OR (
               e.search_vector @@ plainto_tsquery('simple', unaccent(:q))
               OR (e.title || ' ' || COALESCE(e.description, '')) % unaccent(:q)
          ))
        GROUP BY e.id, u.id, u.first_name, u.last_name
    """,
    countQuery = """
        SELECT COUNT(DISTINCT e.id)
        FROM exams e
        WHERE e.privacy = 'PUBLIC'
          AND e.create_method IN ('MANUAL', 'AI')
          AND (:q IS NULL OR :q = '' OR (
               e.search_vector @@ plainto_tsquery('simple', unaccent(:q))
               OR (e.title || ' ' || COALESCE(e.description, '')) % unaccent(:q)
          ))
    """,
    nativeQuery = true)
    Page<SocialExamProjection> findSocialExams(String q, Pageable pageable);

    long countByCreatedBy(Long createdBy);
}

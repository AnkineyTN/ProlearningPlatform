package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.dto.helper.LabelCountProjection;
import com.cabybara.prolearningplatform.enums.AccountType;
import com.cabybara.prolearningplatform.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    Page<User> findByIsBlocked(boolean isBlocked, Pageable pageable);

    @Query("""
        SELECT u FROM User u
        WHERE (:#{#keyword == null} = true
            OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.lastName)  LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.email)     LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:#{#accountType == null} = true OR u.accountType = :accountType)
    """)
    Page<User> searchForAdmin(
        @Param("keyword")     String keyword,
        @Param("accountType") AccountType accountType,
        Pageable pageable
    );

    @Query(value = """
            SELECT COALESCE(CAST(u.education AS text), 'UNSET') AS label,
                   CAST(COUNT(*) AS bigint)                     AS count
            FROM users u
            GROUP BY u.education
            ORDER BY COUNT(*) DESC
            """, nativeQuery = true)
    List<LabelCountProjection> aggregateEducation();

    @Query(value = """
            SELECT COALESCE(CAST(u.hear_app_from AS text), 'UNSET') AS label,
                   CAST(COUNT(*) AS bigint)                          AS count
            FROM users u
            GROUP BY u.hear_app_from
            ORDER BY COUNT(*) DESC
            """, nativeQuery = true)
    List<LabelCountProjection> aggregateHearAppFrom();

    @Query(value = """
            SELECT u.account_type          AS label,
                   CAST(COUNT(*) AS bigint) AS count
            FROM users u
            GROUP BY u.account_type
            ORDER BY COUNT(*) DESC
            """, nativeQuery = true)
    List<LabelCountProjection> aggregateAccountType();

    // bỏ qua những user đã là ACTIVE member của note (cho phép re-invite pending members)
    @Query("""
    SELECT u FROM User u
    WHERE (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND u.id NOT IN (
        SELECT nm.user.id FROM NoteMember nm WHERE nm.note.id = :noteId AND nm.status = 'ACTIVE'
    )
    ORDER BY u.lastName ASC, u.firstName ASC
    """)
   Page<User> searchByNameOrEmail(@Param("keyword") String keyword, @Param("noteId") Long noteId, Pageable pageable);

    // bỏ qua những user đã là ACTIVE member của flashcard (cho phép re-invite pending members)
    @Query("""
    SELECT u FROM User u
    WHERE (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND u.id NOT IN (
        SELECT fm.user.id FROM FlashcardMember fm WHERE fm.flashcard.id = :flashcardId AND fm.status = 'ACTIVE'
    )
    ORDER BY u.lastName ASC, u.firstName ASC
    """)
   Page<User> searchByNameOrEmailForFlashcard(@Param("keyword") String keyword, @Param("flashcardId") Long flashcardId, Pageable pageable);

    // bỏ qua những user đã là ACTIVE member của exam đó (cho phép re-invite pending members)
    @Query("""
    SELECT u FROM User u
    WHERE (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND u.id NOT IN (
        SELECT em.user.id FROM ExamMember em WHERE em.exam.id = :examId AND em.status = 'ACTIVE'
    )
    ORDER BY u.lastName ASC, u.firstName ASC
    """)
   Page<User> searchByNameOrEmailForExam(@Param("keyword") String keyword, @Param("examId") Long examId, Pageable pageable);

    // lấy tất cả users không phải là ACTIVE member của note (cho phép re-invite pending members)
    @Query("""
    SELECT u FROM User u
    WHERE u.id NOT IN (
        SELECT nm.user.id FROM NoteMember nm WHERE nm.note.id = :noteId AND nm.status = 'ACTIVE'
    )
    ORDER BY u.lastName ASC, u.firstName ASC
    """)
    Page<User> findAllExcludingNoteMembers(@Param("noteId") Long noteId, Pageable pageable);

    // lấy tất cả users không phải là ACTIVE member của flashcard (cho phép re-invite pending members)
    @Query("""
    SELECT u FROM User u
    WHERE u.id NOT IN (
        SELECT fm.user.id FROM FlashcardMember fm WHERE fm.flashcard.id = :flashcardId AND fm.status = 'ACTIVE'
    )
    ORDER BY u.lastName ASC, u.firstName ASC
    """)
    Page<User> findAllExcludingFlashcardMembers(@Param("flashcardId") Long flashcardId, Pageable pageable);

    // lấy tất cả users không phải là ACTIVE member của exam (cho phép re-invite pending members)
    @Query("""
    SELECT u FROM User u
    WHERE u.id NOT IN (
        SELECT em.user.id FROM ExamMember em WHERE em.exam.id = :examId AND em.status = 'ACTIVE'
    )
    ORDER BY u.lastName ASC, u.firstName ASC
    """)
    Page<User> findAllExcludingExamMembers(@Param("examId") Long examId, Pageable pageable);

    @Query(value = """
        WITH public_counts AS (
            SELECT id_user AS user_id, 
                   COUNT(*) AS total,
                   COUNT(CASE WHEN created_at >= :since THEN 1 END) AS new_count
            FROM note
            WHERE privacy = 'PUBLIC'
            GROUP BY id_user
            
            UNION ALL
            
            SELECT id_user AS user_id, 
                   COUNT(*) AS total,
                   COUNT(CASE WHEN created_at >= :since THEN 1 END) AS new_count
            FROM flashcard
            WHERE privacy = 'PUBLIC'
            GROUP BY id_user
            
            UNION ALL
            
            SELECT created_by AS user_id, 
                   COUNT(*) AS total,
                   COUNT(CASE WHEN created_at >= :since THEN 1 END) AS new_count
            FROM exams
            WHERE privacy = 'PUBLIC'
            GROUP BY created_by
        ),
        aggregated_counts AS (
            SELECT user_id, 
                   SUM(total) AS total_resources,
                   SUM(new_count) AS new_resources
            FROM public_counts
            GROUP BY user_id
        )
        SELECT u.id, 
               u.first_name, 
               u.last_name, 
               u.email, 
               u.avatar_url, 
               COALESCE(ac.total_resources, 0) AS total_resources,
               COALESCE(ac.new_resources, 0) AS new_resources
        FROM users u
        INNER JOIN aggregated_counts ac ON u.id = ac.user_id
        ORDER BY total_resources DESC, new_resources DESC
    """, nativeQuery = true)
    List<Object[]> findTopCreators(@Param("since") java.time.OffsetDateTime since, Pageable pageable);
}

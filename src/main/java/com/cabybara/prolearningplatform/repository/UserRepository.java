package com.cabybara.prolearningplatform.repository;

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

    @Query(value = """
            SELECT COALESCE(CAST(u.education AS text), 'UNSET'), CAST(COUNT(*) AS bigint)
            FROM users u
            GROUP BY u.education
            ORDER BY COUNT(*) DESC
            """, nativeQuery = true)
    List<Object[]> aggregateEducation();

    @Query(value = """
            SELECT COALESCE(CAST(u.hear_app_from AS text), 'UNSET'), CAST(COUNT(*) AS bigint)
            FROM users u
            GROUP BY u.hear_app_from
            ORDER BY COUNT(*) DESC
            """, nativeQuery = true)
    List<Object[]> aggregateHearAppFrom();

    @Query(value = """
            SELECT u.account_type, CAST(COUNT(*) AS bigint)
            FROM users u
            GROUP BY u.account_type
            ORDER BY COUNT(*) DESC
            """, nativeQuery = true)
    List<Object[]> aggregateAccountType();

    // bỏ qua những user đã là member của note đó
    @Query("""
    SELECT u FROM User u
    WHERE (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND u.id NOT IN (
        SELECT nm.user.id FROM NoteMember nm WHERE nm.note.id = :noteId
    )
    ORDER BY u.lastName ASC, u.firstName ASC
    """)
   Page<User> searchByNameOrEmail(@Param("keyword") String keyword, @Param("noteId") Long noteId, Pageable pageable);

    // bỏ qua những user đã là member của flashcard đó
    @Query("""
    SELECT u FROM User u
    WHERE (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND u.id NOT IN (
        SELECT fm.user.id FROM FlashcardMember fm WHERE fm.flashcard.id = :flashcardId
    )
    ORDER BY u.lastName ASC, u.firstName ASC
    """)
   Page<User> searchByNameOrEmailForFlashcard(@Param("keyword") String keyword, @Param("flashcardId") Long flashcardId, Pageable pageable);

    // bỏ qua những user đã là member của exam đó
    @Query("""
    SELECT u FROM User u
    WHERE (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND u.id NOT IN (
        SELECT em.user.id FROM ExamMember em WHERE em.exam.id = :examId
    )
    ORDER BY u.lastName ASC, u.firstName ASC
    """)
   Page<User> searchByNameOrEmailForExam(@Param("keyword") String keyword, @Param("examId") Long examId, Pageable pageable);

    // lấy tất cả users không phải là member của note
    @Query("""
    SELECT u FROM User u
    WHERE u.id NOT IN (
        SELECT nm.user.id FROM NoteMember nm WHERE nm.note.id = :noteId
    )
    ORDER BY u.lastName ASC, u.firstName ASC
    """)
    Page<User> findAllExcludingNoteMembers(@Param("noteId") Long noteId, Pageable pageable);

    // lấy tất cả users không phải là member của flashcard
    @Query("""
    SELECT u FROM User u
    WHERE u.id NOT IN (
        SELECT fm.user.id FROM FlashcardMember fm WHERE fm.flashcard.id = :flashcardId
    )
    ORDER BY u.lastName ASC, u.firstName ASC
    """)
    Page<User> findAllExcludingFlashcardMembers(@Param("flashcardId") Long flashcardId, Pageable pageable);

    // lấy tất cả users không phải là member của exam
    @Query("""
    SELECT u FROM User u
    WHERE u.id NOT IN (
        SELECT em.user.id FROM ExamMember em WHERE em.exam.id = :examId
    )
    ORDER BY u.lastName ASC, u.firstName ASC
    """)
    Page<User> findAllExcludingExamMembers(@Param("examId") Long examId, Pageable pageable);
}

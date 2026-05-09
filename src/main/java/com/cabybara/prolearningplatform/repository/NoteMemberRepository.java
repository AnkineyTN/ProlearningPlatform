package com.cabybara.prolearningplatform.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.cabybara.prolearningplatform.enums.NoteMemberStatus;
import com.cabybara.prolearningplatform.model.note.NoteMember;

public interface NoteMemberRepository extends JpaRepository<NoteMember, Long> {

    Optional<NoteMember> findByNoteIdAndUserId(Long noteId, Long userId);

    Optional<NoteMember> findByNoteIdAndUserIdAndStatus(
        Long noteId, Long userId, NoteMemberStatus status);

    boolean existsByNoteIdAndUserIdAndStatus(
        Long noteId, Long userId, NoteMemberStatus status);

    List<NoteMember> findByNoteId(Long noteId);

    List<NoteMember> findByUserIdAndStatus(Long userId, NoteMemberStatus status);

    Optional<NoteMember> findByInviteToken(String inviteToken);

    @Modifying
    @Transactional
    @Query("DELETE FROM NoteMember m WHERE m.note.id = :noteId AND m.user.id = :userId")
    void deleteByNoteIdAndUserId(@Param("noteId") Long noteId, @Param("userId") Long userId);

    @Query("""
        SELECT m FROM NoteMember m
        JOIN m.user u
        WHERE m.note.id = :noteId
        AND m.status = 'ACTIVE'
        ORDER BY u.lastName ASC, u.firstName ASC
        """)
    Page<NoteMember> findPagedByNoteId(
        @Param("noteId") Long noteId,
        Pageable pageable
    );

    @Query("""
        SELECT m FROM NoteMember m
        JOIN m.user u
        WHERE m.note.id = :noteId
        AND m.status = 'ACTIVE'
        AND (
            LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        ORDER BY u.lastName ASC, u.firstName ASC
        """)
    Page<NoteMember> searchMembers(
        @Param("noteId") Long noteId,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    // Delete old PENDING/DECLINED records (> 7 days)
    @Modifying
    @Transactional
    @Query("""
        DELETE FROM NoteMember m
        WHERE (m.status = 'PENDING' OR m.status = 'DECLINED')
        AND m.createdAt < :cutoffDate
        """)
    void deleteOldPendingOrDeclinedRecords(@Param("cutoffDate") LocalDateTime cutoffDate);
}
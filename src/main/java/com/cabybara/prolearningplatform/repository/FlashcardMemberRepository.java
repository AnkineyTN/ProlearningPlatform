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

import com.cabybara.prolearningplatform.enums.FlashcardMemberStatus;
import com.cabybara.prolearningplatform.model.flashcard.FlashcardMember;

public interface FlashcardMemberRepository extends JpaRepository<FlashcardMember, Long> {

    Optional<FlashcardMember> findByFlashcardIdAndUserId(Long flashcardId, Long userId);

    @Query("""
        SELECT m FROM FlashcardMember m
        JOIN FETCH m.user
        WHERE m.flashcard.id = :flashcardId AND m.user.id = :userId
        """)
    Optional<FlashcardMember> findWithUserByFlashcardIdAndUserId(
        @Param("flashcardId") Long flashcardId,
        @Param("userId") Long userId
    );

    @Query("""
        SELECT m FROM FlashcardMember m
        JOIN FETCH m.user
        WHERE m.flashcard.id = :flashcardId AND m.user.id IN :userIds
        """)
    List<FlashcardMember> findWithUserByFlashcardIdAndUserIdIn(
        @Param("flashcardId") Long flashcardId,
        @Param("userIds") List<Long> userIds);

    Optional<FlashcardMember> findByFlashcardIdAndUserIdAndStatus(
        Long flashcardId, Long userId, FlashcardMemberStatus status);

    boolean existsByFlashcardIdAndUserIdAndStatus(
        Long flashcardId, Long userId, FlashcardMemberStatus status);

    List<FlashcardMember> findByFlashcardId(Long flashcardId);

    List<FlashcardMember> findByUserIdAndStatus(Long userId, FlashcardMemberStatus status);

    Optional<FlashcardMember> findByInviteToken(String inviteToken);

    @Modifying
    @Transactional
    @Query("DELETE FROM FlashcardMember m WHERE m.flashcard.id = :flashcardId AND m.user.id = :userId")
    void deleteByFlashcardIdAndUserId(@Param("flashcardId") Long flashcardId, @Param("userId") Long userId);

    @Query("""
        SELECT m FROM FlashcardMember m
        JOIN m.user u
        WHERE m.flashcard.id = :flashcardId
        AND m.status = 'ACTIVE'
        ORDER BY u.lastName ASC, u.firstName ASC
        """)
    Page<FlashcardMember> findPagedByFlashcardId(
        @Param("flashcardId") Long flashcardId,
        Pageable pageable
    );

    @Query("""
        SELECT m FROM FlashcardMember m
        JOIN m.user u
        WHERE m.flashcard.id = :flashcardId
        AND m.status = 'ACTIVE'
        AND (
            LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        ORDER BY u.lastName ASC, u.firstName ASC
        """)
    Page<FlashcardMember> searchMembers(
        @Param("flashcardId") Long flashcardId,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    // Delete old PENDING/DECLINED records (> 7 days)
    @Modifying
    @Transactional
    @Query("""
        DELETE FROM FlashcardMember m
        WHERE (m.status = 'PENDING' OR m.status = 'DECLINED')
        AND m.createdAt < :cutoffDate
        """)
    void deleteOldPendingOrDeclinedRecords(@Param("cutoffDate") LocalDateTime cutoffDate);
}

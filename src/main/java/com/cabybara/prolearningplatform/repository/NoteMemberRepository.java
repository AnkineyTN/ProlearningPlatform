package com.cabybara.prolearningplatform.repository;

import java.util.List;
import java.util.Optional;

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
}
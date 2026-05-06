package com.cabybara.prolearningplatform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.cabybara.prolearningplatform.enums.ExamMemberStatus;
import com.cabybara.prolearningplatform.model.exam.ExamMember;

public interface ExamMemberRepository extends JpaRepository<ExamMember, Long> {

    Optional<ExamMember> findByExamIdAndUserId(Long examId, Long userId);

    Optional<ExamMember> findByExamIdAndUserIdAndStatus(
        Long examId, Long userId, ExamMemberStatus status);

    boolean existsByExamIdAndUserIdAndStatus(
        Long examId, Long userId, ExamMemberStatus status);

    List<ExamMember> findByExamId(Long examId);

    List<ExamMember> findByUserIdAndStatus(Long userId, ExamMemberStatus status);

    Optional<ExamMember> findByInviteToken(String inviteToken);

    @Modifying
    @Transactional
    @Query("DELETE FROM ExamMember m WHERE m.exam.id = :examId AND m.user.id = :userId")
    void deleteByExamIdAndUserId(@Param("examId") Long examId, @Param("userId") Long userId);

    @Query("""
        SELECT m FROM ExamMember m
        JOIN m.user u
        WHERE m.exam.id = :examId
        AND m.status != 'DECLINED'
        ORDER BY u.lastName ASC, u.firstName ASC
        """)
    Page<ExamMember> findPagedByExamId(
        @Param("examId") Long examId,
        Pageable pageable
    );

    @Query("""
        SELECT m FROM ExamMember m
        JOIN m.user u
        WHERE m.exam.id = :examId
        AND m.status != 'DECLINED'
        AND (
            LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        ORDER BY u.lastName ASC, u.firstName ASC
        """)
    Page<ExamMember> searchMembers(
        @Param("examId") Long examId,
        @Param("keyword") String keyword,
        Pageable pageable
    );
}

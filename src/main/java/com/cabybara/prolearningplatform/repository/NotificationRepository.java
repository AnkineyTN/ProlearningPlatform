package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.noti.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndIsReadFalse(Long userId);

    java.util.Optional<Notification> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId, @Param("now") OffsetDateTime now);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now WHERE n.id IN :ids AND n.user.id = :userId")
    int markAsReadByIds(@Param("ids") List<Long> ids, @Param("userId") Long userId, @Param("now") OffsetDateTime now);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :before AND n.isRead = true")
    int deleteOldReadNotifications(@Param("before") OffsetDateTime before);

    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, com.cabybara.prolearningplatform.enums.NotificationType type, Pageable pageable);

    @Modifying
    @Query(value = "DELETE FROM notification n WHERE n.user_id = :userId AND n.type = :type AND n.data->>'flashcardId' = :flashcardId", nativeQuery = true)
    int deleteFlashcardInviteNotification(@Param("userId") Long userId, @Param("type") String type, @Param("flashcardId") String flashcardId);

    @Modifying
    @Query(value = "DELETE FROM notification n WHERE n.user_id = :userId AND n.type = :type AND n.data->>'examId' = :examId", nativeQuery = true)
    int deleteExamInviteNotification(@Param("userId") Long userId, @Param("type") String type, @Param("examId") String examId);

    @Modifying
    @Query(value = "DELETE FROM notification n WHERE n.user_id = :userId AND n.type = :type AND n.data->>'noteId' = :noteId", nativeQuery = true)
    int deleteNoteInviteNotification(@Param("userId") Long userId, @Param("type") String type, @Param("noteId") String noteId);
}


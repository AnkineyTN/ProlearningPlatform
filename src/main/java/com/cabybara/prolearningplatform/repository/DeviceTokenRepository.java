package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.fcm.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    Optional<DeviceToken> findByToken(String token);

    @Query("SELECT d.token FROM DeviceToken d WHERE d.user.id = :userId")
    List<String> findAllTokensByUserId(@Param("userId") Long userId);

    @Query("SELECT d.token FROM DeviceToken d WHERE d.user.id IN :userIds")
    List<String> findAllTokensByUserIdIn(@Param("userIds") List<Long> userIds);

    @Modifying
    @Transactional
    @Query("DELETE FROM DeviceToken d WHERE d.token = :token")
    void deleteByToken(@Param("token") String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM DeviceToken d WHERE d.token IN :failedTokens")
    void deleteAllByTokenIn(@Param("failedTokens") List<String> failedTokens);

    @Modifying
    @Transactional
    @Query("DELETE FROM DeviceToken d WHERE d.user.id = :userId AND d.deviceId = :deviceId")
    void deleteByUserIdAndDeviceId(@Param("userId") Long userId, @Param("deviceId") String deviceId);
}

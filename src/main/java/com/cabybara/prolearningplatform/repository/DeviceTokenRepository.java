package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.fcm.DeviceToken;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    Optional<DeviceToken> findByToken(String token);

    @Query("SELECT d.token FROM DeviceToken d WHERE d.user.id = :userId")
    List<String> findAllTokensByUserId(@Param("userId") Long userId);

    @Query("SELECT d.token FROM DeviceToken d WHERE d.user.id IN :userIds")
    List<String> findAllTokensByUserIdIn(@Param("userIds") List<Long> userIds);

    void deleteByToken(String token);

    void deleteAllByTokenIn(List<String> failedTokens);
}

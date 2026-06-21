package com.cabybara.prolearningplatform.repository.llm;

import com.cabybara.prolearningplatform.model.llm.UserLlmConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserLlmConfigRepository extends JpaRepository<UserLlmConfig, Long> {

    List<UserLlmConfig> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<UserLlmConfig> findByIdAndUserId(Long id, Long userId);

    Optional<UserLlmConfig> findByUserIdAndActiveTrue(Long userId);

    long countByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("update UserLlmConfig c set c.active = false where c.user.id = :userId and c.active = true")
    void deactivateAll(@Param("userId") Long userId);
}

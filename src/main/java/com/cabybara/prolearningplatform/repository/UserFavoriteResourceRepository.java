package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.enums.ContentType;
import com.cabybara.prolearningplatform.model.UserFavoriteResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserFavoriteResourceRepository extends JpaRepository<UserFavoriteResource, Long> {

    Optional<UserFavoriteResource> findByUserIdAndResourceIdAndResourceType(Long userId, Long resourceId, ContentType resourceType);

    boolean existsByUserIdAndResourceIdAndResourceType(Long userId, Long resourceId, ContentType resourceType);

    Page<UserFavoriteResource> findByUserIdAndResourceType(Long userId, ContentType resourceType, Pageable pageable);

    Page<UserFavoriteResource> findByUserId(Long userId, Pageable pageable);
}

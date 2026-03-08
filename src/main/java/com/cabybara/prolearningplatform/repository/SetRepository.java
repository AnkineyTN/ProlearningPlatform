package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.Set;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SetRepository extends JpaRepository<Set, Long> {
    Optional<Set> findByTitle(String title);

    boolean existsByTitle(String title);

    Page<Set> findAllByUserId(Long userId, Pageable pageable);

    boolean existsByTitleAndIdNot(String title, Long id);

    @Modifying
    @Query("UPDATE Set s SET s.updatedAt = :now WHERE s.id = :id")
    void updateLastModifiedDate(@Param("id") Long id, @Param("now") OffsetDateTime now);


}

package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.Set;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SetRepository extends JpaRepository<Set, Long> {

    Optional<Set> findByTitle(String title);

    boolean existsByTitle(String title);

    Page<Set> findAllByUserId(Long userId, Pageable pageable);

    boolean existsByTitleAndIdNot(String title, Long id);
}

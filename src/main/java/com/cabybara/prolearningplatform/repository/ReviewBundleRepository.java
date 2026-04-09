package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.review.ReviewBundle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewBundleRepository extends JpaRepository<ReviewBundle, Long> {

    Optional<ReviewBundle> findByIdAndUserId(Long id, Long userId);
}

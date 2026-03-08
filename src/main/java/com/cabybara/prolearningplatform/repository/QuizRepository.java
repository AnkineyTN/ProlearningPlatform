package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.exam.Quiz;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz,Long> {

    Optional<Quiz> findBySetIdAndTitle(Long setId, String title);

    @Query("select (count(q) > 0) from Quiz q where q.title = ?1 and q.set = ?2")
    boolean existsByTitleAndSet(String title, Set set);

    List<Quiz> findAllBySet(Set set, Pageable pageable);
}

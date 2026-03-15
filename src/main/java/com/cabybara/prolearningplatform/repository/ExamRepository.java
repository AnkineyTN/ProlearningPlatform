package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.exam.Exam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam,Long> {

    @Query("select (count(q) > 0) from Exam q where q.title = ?1 and q.set = ?2")
    boolean existsByTitleAndSet(String title, Set set);

    List<Exam> findAllBySet(Set set, Pageable pageable);

    @Query(
            value = "SELECT * FROM exams q WHERE q.set_id = :setId AND q.id = :examId",
            nativeQuery = true
    )
    Optional<Exam> findBySetIdAndId(Long setId, Long examId);

    boolean existsBySetIdAndId(Long setId, Long examId);
}

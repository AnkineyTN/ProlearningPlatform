package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SetRepository extends JpaRepository<Set, Long> {

}

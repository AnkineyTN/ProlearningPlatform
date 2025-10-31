package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.CardItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardItemRepository extends JpaRepository<CardItem, Long> {
}

package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    @Query(value = """
            SELECT COALESCE(CAST(u.education AS text), 'UNSET'), CAST(COUNT(*) AS bigint)
            FROM users u
            GROUP BY u.education
            ORDER BY COUNT(*) DESC
            """, nativeQuery = true)
    List<Object[]> aggregateEducation();

    @Query(value = """
            SELECT COALESCE(CAST(u.hear_app_from AS text), 'UNSET'), CAST(COUNT(*) AS bigint)
            FROM users u
            GROUP BY u.hear_app_from
            ORDER BY COUNT(*) DESC
            """, nativeQuery = true)
    List<Object[]> aggregateHearAppFrom();

    @Query(value = """
            SELECT u.account_type, CAST(COUNT(*) AS bigint)
            FROM users u
            GROUP BY u.account_type
            ORDER BY COUNT(*) DESC
            """, nativeQuery = true)
    List<Object[]> aggregateAccountType();

    @Query("""
        SELECT u FROM User u
        WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY u.lastName ASC, u.firstName ASC
        LIMIT 10
        """)
    List<User> searchByNameOrEmail(@Param("keyword") String keyword);

    @Query("""
    SELECT u FROM User u
    WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%'))
    ORDER BY u.lastName ASC, u.firstName ASC
    """)
   Page<User> searchByNameOrEmail(@Param("keyword") String keyword, Pageable pageable);
}

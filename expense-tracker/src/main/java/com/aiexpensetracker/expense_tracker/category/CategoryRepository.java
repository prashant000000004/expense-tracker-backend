package com.aiexpensetracker.expense_tracker.category;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {
//    get all categories for a specific user
    List<Category> findByUserId(Long userId);
//    check if user have already that category name
    boolean existsByNameAndUserId(String name, Long userId);
//    find specific category belonging to user
    Optional<Category> findByIdAndUserId(Long id, Long userId);
}

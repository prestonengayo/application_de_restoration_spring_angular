package com.quest.etna.repositories;

import com.quest.etna.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

	// Trouver une catégorie par son nom
    Optional<Category> findByName(String name);
    
    // Vérifier si une catégorie existe par son nom
    boolean existsByName(String name);
}

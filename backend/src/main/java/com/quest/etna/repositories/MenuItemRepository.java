package com.quest.etna.repositories;

import com.quest.etna.model.MenuItem;
import com.quest.etna.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {

    // Trouver tous les éléments de menu par catégorie
    List<MenuItem> findByCategory(Category category);

    // Trouver un élément de menu par son nom
    Optional<MenuItem> findByName(String name);

    // Vérifier si un élément de menu existe par son nom
    boolean existsByName(String name);
}

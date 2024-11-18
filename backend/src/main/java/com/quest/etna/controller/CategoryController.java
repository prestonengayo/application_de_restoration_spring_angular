package com.quest.etna.controller;

import com.quest.etna.model.Category;
import com.quest.etna.model.User;
import com.quest.etna.model.UserRole;
import com.quest.etna.repositories.CategoryRepository;
import com.quest.etna.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    // GET: Récupérer toutes les catégories
    @GetMapping("")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.status(HttpStatus.OK).body(categoryRepository.findAll());
    }

    // GET: Récupérer une catégorie par son ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable("id") Integer id) {
        Optional<Category> category = categoryRepository.findById(id);
        return category.<ResponseEntity<?>>map(cat -> ResponseEntity.status(HttpStatus.OK).body(cat))
                       .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found"));
    }

    // POST: Créer une nouvelle catégorie (Admin uniquement)
    @PostMapping("")
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        User authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser.getRole() != UserRole.ROLE_ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only administrators can create categories");
        }
        categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    // PUT: Modifier une catégorie (Admin uniquement)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable("id") Integer id, @RequestBody Category updatedCategory) {
        User authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser.getRole() != UserRole.ROLE_ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only administrators can update categories");
        }

        Optional<Category> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            Category cat = category.get();
            cat.setName(updatedCategory.getName());
            categoryRepository.save(cat);
            return ResponseEntity.status(HttpStatus.OK).body(cat);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found");
        }
    }

	// DELETE: Supprimer une catégorie (Admin uniquement)
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteCategory(@PathVariable("id") Integer id) {
	    User authenticatedUser = getAuthenticatedUser();
	    if (authenticatedUser.getRole() != UserRole.ROLE_ADMIN) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Only administrators can delete categories"));
	    }
	
	    Optional<Category> category = categoryRepository.findById(id);
	    if (category.isPresent()) {
	        categoryRepository.delete(category.get());
	        // Renvoyer une réponse JSON
	        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Category deleted successfully"));
	    } else {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Category not found"));
	    }
	}

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName());
    }

}

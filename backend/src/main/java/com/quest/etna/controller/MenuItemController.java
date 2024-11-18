package com.quest.etna.controller;

import com.quest.etna.dto.MenuItemDTO;
import com.quest.etna.model.Category;
import com.quest.etna.model.MenuItem;
import com.quest.etna.model.User;
import com.quest.etna.model.UserRole;
import com.quest.etna.repositories.CategoryRepository;
import com.quest.etna.repositories.MenuItemRepository;
import com.quest.etna.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/menu-item")
public class MenuItemController {

    @Autowired
    private MenuItemRepository menuItemRepository;
    
    @Autowired
    private CategoryRepository categoryRepository; 
    
    @Autowired
    private com.quest.etna.config.FileStorageService FileStorageService; 

    @Autowired
    private UserRepository userRepository;

    @GetMapping("")
    public ResponseEntity<List<MenuItemDTO>> getMenuItems() {
        List<MenuItem> menuItems = menuItemRepository.findAll();
        List<MenuItemDTO> menuItemDTOs = menuItems.stream().map(this::convertToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(menuItemDTOs);
    }

    // Méthode pour convertir un MenuItem en MenuItemDTO
    private MenuItemDTO convertToDTO(MenuItem menuItem) {
        MenuItemDTO dto = new MenuItemDTO();
        dto.setId(menuItem.getId());
        dto.setName(menuItem.getName());
        dto.setPrice(menuItem.getPrice());
        dto.setDescription(menuItem.getDescription());
        dto.setImageUrl(menuItem.getImageUrl());
        dto.setCategoryName(menuItem.getCategory().getName()); // Utiliser le nom de la catégorie
        return dto;
    }
    

    // GET: Récupérer un article de menu par son ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getMenuItemById(@PathVariable("id") Integer id) {
        Optional<MenuItem> menuItem = menuItemRepository.findById(id);
        return menuItem.<ResponseEntity<?>>map(item -> ResponseEntity.status(HttpStatus.OK).body(item))
                       .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Menu item not found"));
    }
	
    
 // POST: Créer un nouvel article de menu (Admin uniquement)
   /* @PostMapping("")
    public ResponseEntity<?> createMenuItem(@RequestBody MenuItem menuItem) {
        User authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser.getRole() != UserRole.ROLE_ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only administrators can create menu items");
        }

        // Rechercher la catégorie par son nom en utilisant l'instance categoryRepository
        Optional<Category> categoryOptional = categoryRepository.findByName(menuItem.getCategory().getName());
        if (categoryOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Category not found");
        }

        // Associer la catégorie trouvée au MenuItem
        menuItem.setCategory(categoryOptional.get());

        // Sauvegarder le MenuItem avec la catégorie associée
        menuItemRepository.save(menuItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(menuItem);
    }
*/
    
    @PostMapping(value = "", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createMenuItem(
        @RequestParam("name") String name,
        @RequestParam("price") Double price,
        @RequestParam("description") String description,
        @RequestParam("category") String categoryName,
        @RequestParam("image") MultipartFile image) {
        
        User authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser.getRole() != UserRole.ROLE_ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only administrators can create menu items");
        }

        // Rechercher la catégorie par son nom
        Optional<Category> categoryOptional = categoryRepository.findByName(categoryName);
        if (categoryOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Category not found");
        }

        // Sauvegarder l'image dans un répertoire sur le serveur et obtenir l'URL
        String imageUrl = FileStorageService.storeFile(image); // Implémentez cette méthode pour gérer l'enregistrement de fichiers.

        // Créer le nouvel élément de menu
        MenuItem menuItem = new MenuItem();
        menuItem.setName(name);
        menuItem.setPrice(price);
        menuItem.setDescription(description);
        menuItem.setImageUrl(imageUrl);
        menuItem.setCategory(categoryOptional.get());

        // Sauvegarder l'élément de menu
        menuItemRepository.save(menuItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(menuItem);
    }

    
    

 // PUT: Mettre à jour un article de menu (Admin uniquement)
   /* @PutMapping("/{id}")
    public ResponseEntity<?> updateMenuItem(@PathVariable("id") Integer id, @RequestBody MenuItem updatedMenuItem) {
        User authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser.getRole() != UserRole.ROLE_ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only administrators can update menu items");
        }

        Optional<MenuItem> menuItemOptional = menuItemRepository.findById(id);
        if (menuItemOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Menu item not found");
        }

        MenuItem menuItem = menuItemOptional.get();

        // Rechercher la catégorie par son nom en utilisant l'instance categoryRepository
        Optional<Category> categoryOptional = categoryRepository.findByName(updatedMenuItem.getCategory().getName());
        if (categoryOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Category not found");
        }

        // Associer la catégorie trouvée au MenuItem
        menuItem.setCategory(categoryOptional.get());

        // Mettre à jour les autres champs du MenuItem
        menuItem.setName(updatedMenuItem.getName());
        menuItem.setPrice(updatedMenuItem.getPrice());
        menuItem.setDescription(updatedMenuItem.getDescription());
        menuItem.setImageUrl(updatedMenuItem.getImageUrl());

        // Sauvegarder les modifications
        menuItemRepository.save(menuItem);
        return ResponseEntity.status(HttpStatus.OK).body(menuItem);
    }
*/
    
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateMenuItem(
        @PathVariable("id") Integer id,
        @RequestParam("name") String name,
        @RequestParam("price") Double price,
        @RequestParam("description") String description,
        @RequestParam("category") String categoryName,
        @RequestParam(value = "image", required = false) MultipartFile image) {

        User authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser.getRole() != UserRole.ROLE_ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only administrators can update menu items");
        }

        Optional<MenuItem> menuItemOptional = menuItemRepository.findById(id);
        if (menuItemOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Menu item not found");
        }

        MenuItem menuItem = menuItemOptional.get();

        // Rechercher la catégorie par son nom
        Optional<Category> categoryOptional = categoryRepository.findByName(categoryName);
        if (categoryOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Category not found");
        }

        // Si une nouvelle image est téléchargée, la sauvegarder
        if (image != null && !image.isEmpty()) {
            String imageUrl = FileStorageService.storeFile(image); // Implémentez cette méthode pour gérer l'enregistrement de fichiers.
            menuItem.setImageUrl(imageUrl); // Mettre à jour l'URL de l'image
        }

        // Mettre à jour les autres champs de l'élément de menu
        menuItem.setName(name);
        menuItem.setPrice(price);
        menuItem.setDescription(description);
        menuItem.setCategory(categoryOptional.get());

        // Sauvegarder les modifications
        menuItemRepository.save(menuItem);
        return ResponseEntity.status(HttpStatus.OK).body(menuItem);
    }

    
    
    
    
    // DELETE: Supprimer un article de menu (Admin uniquement)
@DeleteMapping("/{id}")
public ResponseEntity<Map<String, String>> deleteMenuItem(@PathVariable("id") Integer id) {
    User authenticatedUser = getAuthenticatedUser();
    if (authenticatedUser.getRole() != UserRole.ROLE_ADMIN) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Only administrators can delete menu items");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    Optional<MenuItem> menuItem = menuItemRepository.findById(id);
    if (menuItem.isPresent()) {
        menuItemRepository.delete(menuItem.get());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Menu item deleted successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    } else {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Menu item not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
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

package com.quest.etna.controller;

import com.quest.etna.model.User;
import com.quest.etna.model.UserDetails;
import com.quest.etna.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    
    private static final String UPLOAD_DIR = "uploads/profile_pictures/";
    

    @PostMapping("/{id}/uploadProfilePicture")
    public ResponseEntity<?> uploadProfilePicture(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        try {
            String directory = "uploads/profile_pictures/";
            String fileName = id + "_" + file.getOriginalFilename();
            Path targetLocation = Paths.get(directory + fileName);

            // Remplacer l'existant
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Mettre à jour le profil utilisateur avec l'URL du fichier
            User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            String fileUrl = "/uploads/profile_pictures/" + fileName; // URL relative
            user.setPhotoUrl(fileUrl);
            userRepository.save(user);

            // Renvoie une réponse JSON avec l'URL de la photo
            Map<String, String> response = new HashMap<>();
            response.put("photoUrl", fileUrl);
            response.put("message", "Photo de profil mise à jour avec succès");
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // En cas d'erreur, renvoie une réponse JSON avec un message d'erreur
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors du téléchargement de la photo : " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    
    // GET: Récupérer la liste des utilisateurs (nécessite d'être authentifié)
    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserDetails>> getAllUsers() {
        List<UserDetails> users = StreamSupport.stream(userRepository.findAll().spliterator(), false)
                                               .map(user -> new UserDetails(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getPhotoUrl(), user.getRole()))
                                               .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

 // GET: Récupérer un utilisateur par son ID (nécessite d'être authentifié)
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> getUserById(@PathVariable("id") Integer id) {
        Optional<User> user = userRepository.findById(id.longValue());
        return user.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }


    // PUT: Modifier le rôle, le nom d'utilisateur, ou la photo de profil d'un utilisateur
	@PutMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<User> updateUser(@PathVariable("id") Integer id, @RequestBody User updatedUser) {
	    Optional<User> userOptional = userRepository.findById(id.longValue());
	    if (userOptional.isPresent()) {
	        User user = userOptional.get();
	        User authenticatedUser = getAuthenticatedUser();
	
	        // L'utilisateur peut modifier son propre nom d'utilisateur, prénom, nom et photo de profil
	        if (hasRole(authenticatedUser, "ROLE_ADMIN") || authenticatedUser.getId().equals(user.getId())) {
	            if (updatedUser.getUsername() != null) {
	                user.setUsername(updatedUser.getUsername());
	            }
	            if (updatedUser.getFirstName() != null) {
	                user.setFirstName(updatedUser.getFirstName());
	            }
	            if (updatedUser.getLastName() != null) {
	                user.setLastName(updatedUser.getLastName());
	            }
	            if (updatedUser.getPhotoUrl() != null) {
	                user.setPhotoUrl(updatedUser.getPhotoUrl());
	            }
	
	            // Si un rôle est fourni dans la mise à jour et que l'utilisateur authentifié est un admin, mettre à jour le rôle
	            if (updatedUser.getRole() != null && hasRole(authenticatedUser, "ROLE_ADMIN")) {
	                user.setRole(updatedUser.getRole());
	            } else if (updatedUser.getRole() == null) {
	                // Préserver le rôle actuel si aucun rôle n'est fourni dans la requête
	                user.setRole(user.getRole());
	            }
	
	            userRepository.save(user);
	            return ResponseEntity.ok(user);
	        } else {
	            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	        }
	    } else {
	        return ResponseEntity.notFound().build();
	    }
	}

    
    // DELETE: Supprimer un utilisateur
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Integer id) {
        Optional<User> userOptional = userRepository.findById(id.longValue());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            User authenticatedUser = getAuthenticatedUser();

            // Seul un admin ou l'utilisateur lui-même peut se supprimer
            if (hasRole(authenticatedUser, "ROLE_ADMIN") || authenticatedUser.getId().equals(user.getId())) {
                userRepository.delete(user);
                return ResponseEntity.ok(Collections.singletonMap("success", true));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("success", false));
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Méthodes utilitaires

    // Obtenir l'utilisateur authentifié
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        return userRepository.findByUsername(currentUsername);
    }

    // Vérifier le rôle de l'utilisateur
    private boolean hasRole(User user, String role) {
        return user.getRole().name().equals(role);
    }
}

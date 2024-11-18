package com.quest.etna.controller;

import com.quest.etna.model.Address;
import com.quest.etna.model.JwtUserDetails;
import com.quest.etna.model.User;
import com.quest.etna.model.UserRole;
import com.quest.etna.repositories.AddressRepository;
import com.quest.etna.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    // GET: Récupérer toutes les adresses
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllAddresses() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "JWT token is missing or invalid."));
        }

        User authenticatedUser = getAuthenticatedUser();
        List<Address> addresses;
        if (authenticatedUser.getRole() == UserRole.ROLE_ADMIN) {
            addresses = addressRepository.findAll(); // Admin peut voir toutes les adresses
        } else {
            addresses = addressRepository.findByUserId(authenticatedUser.getId()); // Un utilisateur ne voit que ses propres adresses
        }
        return ResponseEntity.status(HttpStatus.OK).body(addresses);
    }

    // GET: Récupérer une adresse par son ID
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAddressById(@PathVariable("id") int id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "JWT token is missing or invalid."));
        }

        Optional<Address> address = addressRepository.findById(id);
        if (address.isPresent()) {
            User authenticatedUser = getAuthenticatedUser();
            if (isAuthorized(authenticatedUser, address.get())) {
                return ResponseEntity.status(HttpStatus.OK).body(address.get());
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Collections.singletonMap("error", "You do not have permission to access this address"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Address not found"));
        }
    }

// POST: Créer une nouvelle adresse
@PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<?> createAddress(@RequestBody Address address) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Collections.singletonMap("error", "JWT token is missing or invalid."));
    }

    try {
        JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
        User authenticatedUser = userRepository.findByUsername(userDetails.getUsername());

        address.setUser(authenticatedUser);
        addressRepository.save(address);

        return ResponseEntity.status(HttpStatus.CREATED).body(address);
    } catch (ClassCastException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Collections.singletonMap("error", "JWT token is invalid"));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap("error", "Invalid request"));
    }
}

// PUT: Modifier une adresse
@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<?> updateAddress(@PathVariable("id") int id, @RequestBody Address updatedAddress) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Collections.singletonMap("error", "JWT token is missing or invalid."));
    }

    Optional<Address> addressOptional = addressRepository.findById(id);
    if (addressOptional.isPresent()) {
        Address address = addressOptional.get();
        User authenticatedUser = getAuthenticatedUser();
        if (isAuthorized(authenticatedUser, address)) {
            if (updatedAddress.getStreet() != null) address.setStreet(updatedAddress.getStreet());
            if (updatedAddress.getCity() != null) address.setCity(updatedAddress.getCity());
            if (updatedAddress.getPostalCode() != null) address.setPostalCode(updatedAddress.getPostalCode());
            if (updatedAddress.getCountry() != null) address.setCountry(updatedAddress.getCountry());
            
            addressRepository.save(address);
            return ResponseEntity.status(HttpStatus.OK).body(address);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "You do not have permission to update this address"));
        }
    } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("error", "Address not found"));
    }
}

    // DELETE: Supprimer une adresse
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable("id") int id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "JWT token is missing or invalid."));
        }

        Optional<Address> addressOptional = addressRepository.findById(id);
        if (addressOptional.isPresent()) {
            Address address = addressOptional.get();
            User authenticatedUser = getAuthenticatedUser();
            if (isAuthorized(authenticatedUser, address)) {
                addressRepository.delete(address);
                return ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("success", true));
            } else {
                if (authenticatedUser.getRole() == UserRole.ROLE_ADMIN) {
                    addressRepository.delete(address);
                    return ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("success", true));
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Collections.singletonMap("error", "You do not have permission to delete this address"));
                }
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Address not found"));
        }
    }

    // Méthodes utilitaires

    // Obtenir l'utilisateur authentifié
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        return userRepository.findByUsername(currentUsername);
    }

    // Vérifier si l'utilisateur a le droit d'accéder à une adresse
    private boolean isAuthorized(User user, Address address) {
        return user.getId().equals(address.getUser().getId()) || user.getRole() == UserRole.ROLE_ADMIN;
    }
}

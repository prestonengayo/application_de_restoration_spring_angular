package com.quest.etna.controller;

import com.quest.etna.config.JwtTokenUtil;
import com.quest.etna.config.JwtUserDetailsService;
import com.quest.etna.dto.ErrorResponse;
import com.quest.etna.model.JwtRequest;
import com.quest.etna.model.JwtResponse;
import com.quest.etna.model.JwtUserDetails;
import com.quest.etna.model.User;
import com.quest.etna.model.UserRole;
import com.quest.etna.repositories.UserRepository;
import com.quest.etna.model.UserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;
    

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<Object> registerUser(@RequestBody Map<String, String> payload) {
        try {
            String username = payload.get("username");
            String password = payload.get("password");
            String firstName = payload.get("firstName");
            String lastName = payload.get("lastName");
            String photoUrl = payload.get("photoUrl");

            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                ErrorResponse errorResponse = new ErrorResponse("Username and password are required");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            User existingUser = userRepository.findByUsername(username);
            if (existingUser != null) {
                ErrorResponse errorResponse = new ErrorResponse("Username already exists");
                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
            }

            // Chiffrer le mot de passe avant de l'enregistrer
            String encodedPassword = passwordEncoder.encode(password);

            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(encodedPassword);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setPhotoUrl(photoUrl);
            newUser.setRole(UserRole.ROLE_USER);

            userRepository.save(newUser);

            UserDetails userDetails = new UserDetails(
                newUser.getId(),
                newUser.getUsername(),
                newUser.getFirstName(),
                newUser.getLastName(),
                newUser.getPhotoUrl(),
                newUser.getRole()
            );
            return new ResponseEntity<>(userDetails, HttpStatus.CREATED);

        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody JwtRequest authenticationRequest) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("message", "Incorrect username or password"));
        }

        final org.springframework.security.core.userdetails.UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String accessToken = jwtTokenUtil.generateToken(userDetails.getUsername());
        final String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails.getUsername());

        return ResponseEntity.ok(Map.of("accessToken", accessToken, "refreshToken", refreshToken));
    }

    
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> payload) {
        String refreshToken = payload.get("refreshToken");

        // Valider le token
        try {
            String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
            
            // Vérifiez que le token n'a pas expiré
            if (username != null && !jwtTokenUtil.isTokenExpired(refreshToken)) {
                // Générer un nouveau access token
                String newAccessToken = jwtTokenUtil.generateToken(username);
                
                // Générer un nouveau refresh token
                String newRefreshToken = jwtTokenUtil.generateRefreshToken(username); // Supposant que vous avez une méthode pour générer un Refresh Token
                
                // Retourner les deux tokens
                return ResponseEntity.ok(Map.of(
                    "accessToken", newAccessToken,
                    "refreshToken", newRefreshToken
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                     .body(Map.of("message", "Invalid or expired refresh token"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("message", "Invalid or expired refresh token"));
        }
    }



    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                     .body(Map.of("message", "JWT token is missing or invalid"));
            }

            JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();

            String role = userDetails.getAuthorities().stream()
                                     .map(GrantedAuthority::getAuthority)
                                     .findFirst()
                                     .orElse("ROLE_USER");

            User user = userRepository.findByUsername(userDetails.getUsername());
            
            UserDetails responseUserDetails = new UserDetails(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhotoUrl(),
                user.getRole()
            );

            return ResponseEntity.ok(responseUserDetails);

        } catch (ClassCastException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("message", "JWT token is invalid"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(Map.of("message", "Invalid request"));
        }
    }
}

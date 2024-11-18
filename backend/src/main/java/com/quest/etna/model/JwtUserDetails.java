package com.quest.etna.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;


public class JwtUserDetails implements UserDetails {

    private final Integer id;
    private final String username;
    private final String password;
    private final UserRole role;

    // Constructeur avec l'objet User
    public JwtUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.role = user.getRole();
    }

    // Implémentation des méthodes de l'interface UserDetails

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }
    

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Implémentation simple, peut être modifiée selon les besoins
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Implémentation simple, peut être modifiée selon les besoins
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Implémentation simple, peut être modifiée selon les besoins
    }

    @Override
    public boolean isEnabled() {
        return true; // Implémentation simple, peut être modifiée selon les besoins
    }

    // Méthode supplémentaire pour récupérer l'ID utilisateur
    public Integer getId() {
        return id;
    }
    


}

package com.mora.digitalwalletledgerservice.service;


import com.mora.digitalwalletledgerservice.domain.AppUser;
import com.mora.digitalwalletledgerservice.repository.AppUserRepository;
import com.mora.digitalwalletledgerservice.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService implements UserDetailsService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AppUserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        String normalizedEmail = normalizeEmail(email);

        AppUser appUser = userRepository.findByEmail(normalizedEmail).orElseThrow(() ->
                new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));

        return User.withUsername(appUser.getEmail())
                .password(appUser.getPassword())
                .authorities(appUser.getRole())
                .build();
    }


    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
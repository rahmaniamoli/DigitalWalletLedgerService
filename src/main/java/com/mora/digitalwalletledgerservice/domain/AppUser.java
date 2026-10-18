package com.mora.digitalwalletledgerservice.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String role = "ROLE_USER";


    protected AppUser() {
    }

    public AppUser(String email, String password) {
        this.email = email;
        this.password = password;
    }


    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }
}
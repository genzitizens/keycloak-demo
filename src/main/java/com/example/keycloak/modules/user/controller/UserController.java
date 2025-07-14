package com.example.keycloak.modules.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping
    @PreAuthorize("hasRole('role_user') or hasRole('role_admin')")
    public ResponseEntity<String> getUsers() {
        return ResponseEntity.ok("List of users");
    }

    @PostMapping
    @PreAuthorize("hasRole('role_admin')")
    public ResponseEntity<String> getAdminUsers() {
        return ResponseEntity.ok("List of admin users");
    }
}

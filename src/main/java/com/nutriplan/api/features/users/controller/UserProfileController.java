package com.nutriplan.api.features.users.controller;

import com.nutriplan.api.features.users.domain.UserProfile;
import com.nutriplan.api.features.users.services.UserProfileService;
import com.nutriplan.api.features.users.dto.CreateProfileRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;

    /**
     * Crear el perfil inicial del usuario.
     */
    @PostMapping
    public ResponseEntity<UserProfile> createProfile(@Valid @RequestBody CreateProfileRequest request) {
        UserProfile created = profileService.createProfile(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Obtener los datos del perfil del usuario autenticado (incluye macros y kcal).
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfile> getMyProfile() {
        return ResponseEntity.ok(profileService.getMyProfile());
    }

    /**
     * Actualizar los datos del perfil.
     * Recalcula automáticamente macros y guarda histórico de peso si cambia.
     */
    @PutMapping("/me")
    public ResponseEntity<UserProfile> updateProfile(@Valid @RequestBody CreateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(request));
    }

    /**
     * Eliminar el perfil del usuario.
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteProfile() {
        profileService.deleteProfile();
        return ResponseEntity.noContent().build();
    }
}
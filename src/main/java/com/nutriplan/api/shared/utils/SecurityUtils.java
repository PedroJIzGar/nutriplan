package com.nutriplan.api.shared.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.UUID;

public class SecurityUtils {

    /**
     * Extrae el ID del usuario (campo 'sub' del JWT de Supabase)
     * del contexto de seguridad actual.
     */
    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            // Supabase guarda el UUID del usuario en el claim "sub"
            String userIdString = jwt.getSubject();
            return UUID.fromString(userIdString);
        }

        throw new IllegalStateException("No se encontró un usuario autenticado en el contexto de seguridad");
    }
}
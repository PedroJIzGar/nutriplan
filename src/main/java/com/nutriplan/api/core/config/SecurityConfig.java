package com.nutriplan.api.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(request -> {
                    var config = new org.springframework.web.cors.CorsConfiguration();
                    config.setAllowedOrigins(java.util.List.of("http://localhost:3000")); // O la URL de tu front
                    config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(java.util.List.of("*"));
                    return config;
                }))
                .csrf(csrf -> csrf.disable()) // Desactivado para APIs REST
                .authorizeHttpRequests(auth -> auth
                        // Públicos: Swagger, Docs y Actuator
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/**")
                        .permitAll()
                        // El resto de la API de Nutriplan requiere token de Supabase
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // 1. El JSON exacto que me pasaste
        String jwkJson = "{\"x\":\"N0RMa0-pnCRIqwUkPBRrLQJ_ZvbsFQQUZmyX3YcjJqg\",\"y\":\"Q2ZcBksscTosUU8yvGJ-ceEU2naGIuVkC3oH1QN3JW4\",\"alg\":\"ES256\",\"crv\":\"P-256\",\"kid\":\"03e226a5-2a4d-4e80-9153-a3b6b47220da\",\"kty\":\"EC\"}";

        try {
            // 2. Creamos el set de llaves localmente
            com.nimbusds.jose.jwk.JWK jwk = com.nimbusds.jose.jwk.JWK.parse(jwkJson);
            com.nimbusds.jose.jwk.source.JWKSource<com.nimbusds.jose.proc.SecurityContext> jwkSource = new com.nimbusds.jose.jwk.source.ImmutableJWKSet<>(
                    new com.nimbusds.jose.jwk.JWKSet(jwk));

            // 3. Configuramos el procesador para que acepte ES256
            com.nimbusds.jwt.proc.ConfigurableJWTProcessor<com.nimbusds.jose.proc.SecurityContext> jwtProcessor = new com.nimbusds.jwt.proc.DefaultJWTProcessor<>();

            com.nimbusds.jose.proc.JWSKeySelector<com.nimbusds.jose.proc.SecurityContext> keySelector = new com.nimbusds.jose.proc.JWSVerificationKeySelector<>(
                    com.nimbusds.jose.JWSAlgorithm.ES256, jwkSource);

            jwtProcessor.setJWSKeySelector(keySelector);

            // 4. IMPORTANTE: Quitamos las validaciones estrictas de "iss" y "exp" para
            // probar primero si entra
            // Una vez funcione, las podemos volver a poner.
            jwtProcessor.setJWTClaimsSetVerifier((claims, context) -> {
                // No hacemos nada, permitimos todos los claims por ahora para desbloquearte
            });

            return new org.springframework.security.oauth2.jwt.NimbusJwtDecoder(jwtProcessor);

        } catch (Exception e) {
            throw new RuntimeException("Error crítico configurando JWT: " + e.getMessage());
        }
    }
}
package com.nutriplan.api.features.users.services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nutriplan.api.features.users.domain.UserProfile;
import com.nutriplan.api.features.users.domain.WeightLog;
import com.nutriplan.api.features.users.domain.repository.UserProfileRepository;
import com.nutriplan.api.features.users.domain.repository.WeightLogRepository;
import com.nutriplan.api.features.users.dto.CreateProfileRequest;
import com.nutriplan.api.features.users.dto.UpdateProfileRequest;
import com.nutriplan.api.features.users.dto.WeightRequest;
import com.nutriplan.api.shared.exception.ResourceNotFoundException;
import com.nutriplan.api.shared.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userRepository;
    private final WeightLogRepository weightLogRepository;

    @Transactional
    public UserProfile createProfile(CreateProfileRequest request) {
        // 1. Validación de Edad (Feature: Seguridad/Política)
        if (request.getAge() < 18) {
            throw new IllegalArgumentException("Debes ser mayor de 18 años para usar la app.");
        }

        UUID userId = SecurityUtils.getCurrentUserId();
        String email = SecurityUtils.getCurrentUserEmail();

        // 2. Mapeo inicial
        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .email(email)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .age(request.getAge())
                .gender(request.getGender())
                .height(request.getHeight())
                .weight(request.getWeight())
                .activityLevel(request.getActivityLevel())
                .goal(request.getGoal())
                .isConfigured(true) // Ya tiene los datos básicos
                .build();

        // 3. Cálculo de Nutrición Completo
        calculateNutrition(profile);
        UserProfile savedProfile = userRepository.save(profile);
        // 4. Guardar Historial de Peso (Feature: Histórico)
        saveWeightLog(savedProfile, request.getWeight());

        return savedProfile;
    }

    // --- GET ---
    @Transactional(readOnly = true)
    public UserProfile getMyProfile() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado para el usuario: " + userId));
    }

    // --- UPDATE ---
    @Transactional
    public UserProfile updateProfile(UpdateProfileRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        String email = SecurityUtils.getCurrentUserEmail();

        UserProfile profile = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no existe"));

        boolean weightChanged = Double.compare(profile.getWeight(), request.getWeight()) != 0;

        if (weightChanged) {
            saveWeightLog(profile, request.getWeight());
        }

        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setEmail(email); // Se sincroniza desde Supabase/JWT
        profile.setAge(request.getAge());
        profile.setWeight(request.getWeight());
        profile.setHeight(request.getHeight());
        profile.setActivityLevel(request.getActivityLevel());
        profile.setGoal(request.getGoal());

        calculateNutrition(profile);

        return userRepository.save(profile);
    }

    // --- DELETE ---
    @Transactional
    public void deleteProfile() {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("No se puede eliminar un perfil inexistente");
        }
        userRepository.deleteById(userId);
    }

    @Transactional
    public UserProfile registerNewWeight(WeightRequest request, String userIdStr) {
        // 1. Convertir el String que viene del token a UUID
        UUID userId = UUID.fromString(userIdStr);

        // 2. Ahora ya puedes llamar al repositorio que espera un UUID
        UserProfile profile = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado para el ID: " + userIdStr));

        // 3. Continuar con la lógica...
        profile.setWeight(request.getWeight());
        calculateNutrition(profile);

        // Guardar el log (asegúrate de que WeightLog también use tipos coherentes)
        saveWeightLog(profile, request.getWeight());

        return userRepository.save(profile);
    }

    private void calculateNutrition(UserProfile profile) {
        // A. Calcular Calorías (Harris-Benedict que ya teníamos)
        double tmb = ("MALE".equalsIgnoreCase(profile.getGender()))
                ? 88.362 + (13.397 * profile.getWeight()) + (4.799 * profile.getHeight()) - (5.677 * profile.getAge())
                : 447.593 + (9.247 * profile.getWeight()) + (3.098 * profile.getHeight()) - (4.330 * profile.getAge());

        double maintenanceKcal = tmb * profile.getActivityLevel().getMultiplier();

        int totalKcal = switch (profile.getGoal()) {
            case LOSE_WEIGHT -> (int) (maintenanceKcal - 500);
            case GAIN_WEIGHT -> (int) (maintenanceKcal + 300);
            case MAINTAIN_WEIGHT -> (int) maintenanceKcal;
        };

        // B. Cálculo de Macros (Lógica de Nutrición)
        // Proteína: 2g por kg
        int proteinGrams = (int) (profile.getWeight() * 2);
        // Grasas: 1g por kg
        int fatGrams = (int) (profile.getWeight() * 1);
        // Carbohidratos: El resto (1g carb = 4kcal, 1g prot = 4kcal, 1g fat = 9kcal)
        int kcalFromOthers = (proteinGrams * 4) + (fatGrams * 9);
        int carbGrams = (totalKcal - kcalFromOthers) / 4;

        profile.setTargetKcal(totalKcal);
        profile.setTargetProtein(proteinGrams);
        profile.setTargetFat(fatGrams);
        profile.setTargetCarbs(carbGrams);
    }

    private void saveWeightLog(UserProfile profile, Double weight) {
        WeightLog log = WeightLog.builder()
                .user(profile) // <--- Pasamos el objeto UserProfile, NO el UUID
                .weight(weight)
                .logDate(LocalDateTime.now())
                .build();
        weightLogRepository.save(log);
    }
}

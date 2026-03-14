# 🍎 NutriPlan API - Smart Nutrition Engine

Backend robusto para la gestión de planes nutricionales, cálculo dinámico de macronutrientes y seguimiento de peso corporal. Desarrollado con **Java 21** y **Spring Boot 4.0.3**.

## 🚀 Funcionalidades Principales

* **Perfil Antropométrico:** Gestión completa de datos del usuario (edad, altura, peso, actividad).
* **Cálculo Automático de Macros:** * **Calorías ($TDEE$):** Ajustadas según el nivel de actividad y el objetivo (`LOSE_WEIGHT`, `MAINTAIN`, `GAIN_MUSCLE`).
    * **Proteínas:** Calculadas a $2.0\text{g/kg}$ de peso corporal.
    * **Grasas:** Calculadas a $1.0\text{g/kg}$ de peso corporal.
    * **Carbohidratos:** Ajuste dinámico del resto de calorías disponibles.
* **Seguimiento de Peso:** Historial de pesajes (`WeightLogs`) con relación bidireccional optimizada.
* **Arquitectura Anti-Recursión:** Implementación de `@JsonManagedReference` y `@JsonBackReference` para garantizar JSONs limpios y eficientes.

## 🛠️ Stack Tecnológico

* **Lenguaje:** Java 21
* **Framework:** Spring Boot 4.0.3
* **Persistencia:** Spring Data JPA / Hibernate
* **Serialización:** Jackson
* **Base de Datos:** PostgreSQL

## 📊 Ejemplo de Respuesta (Perfil de Usuario)

El sistema devuelve un objeto consolidado listo para el frontend, evitando redundancias:

```json
{
  "userId": "0b342ef5-eec7-4e9a-ad21-45815544b9a8",
  "firstName": "Pedro",
  "targetKcal": 1962,
  "targetProtein": 150,
  "targetCarbs": 171,
  "targetFat": 75,
  "weightLogs": [
    {
      "id": "da800ee1-9483-42b9-b113-e1053135b2bd",
      "weight": 75.0,
      "logDate": "2026-03-14T21:36:26"
    }
  ],
  "configured": true
}

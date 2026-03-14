NutriPlan API - README
======================

Proyecto
--------
NutriPlan API es un backend para la gestión de planes nutricionales y el seguimiento de peso corporal.
Según el repositorio, el proyecto está desarrollado con Java 21 y Spring Boot 4.0.3.

Descripción general
-------------------
La API está planteada como un backend para:
- gestionar el perfil antropométrico del usuario,
- calcular calorías y macronutrientes,
- registrar el historial de peso,
- exponer datos listos para consumir desde frontend.

En el README original del repositorio se indica que el sistema calcula:
- calorías objetivo (TDEE) según nivel de actividad y objetivo,
- proteínas a 2.0 g/kg,
- grasas a 1.0 g/kg,
- carbohidratos de forma dinámica con las calorías restantes.

Objetivos soportados que aparecen en el README original:
- LOSE_WEIGHT
- MAINTAIN
- GAIN_MUSCLE

Stack tecnológico
-----------------
Confirmado en el repositorio:
- Java 21
- Spring Boot 4.0.3
- Spring Data JPA / Hibernate
- Jackson
- PostgreSQL
- Redis
- Flyway
- Spring Security
- OAuth2 Resource Server
- Springdoc OpenAPI UI
- Spring Modulith
- MapStruct
- Lombok
- Spring AI Azure OpenAI starter
- Testcontainers

Estructura observada
--------------------
En el árbol del repositorio se observan estas rutas principales:

- .mvn/
- src/
  - main/
    - java/com/nutriplan/api/
      - core/
      - features/users/
      - shared/
      - NutriplanApiApplication.java
    - resources/
  - test/java/com/nutriplan/api/
- docker-compose.yml
- pom.xml
- mvnw
- mvnw.cmd

Esto sugiere una organización modular, con separación entre núcleo, funcionalidad de usuarios y componentes compartidos.

Arranque de infraestructura local
---------------------------------
El archivo docker-compose.yml del repositorio levanta:

1. PostgreSQL 16 Alpine
   - contenedor: nutriplan-db
   - puerto: 5432
   - base de datos: nutriplan_db
   - usuario: nutriuser
   - contraseña: nutripassword

2. Redis 7 Alpine
   - contenedor: nutriplan-cache
   - puerto: 6379

Comando recomendado:

  docker compose up -d

Configuración detectada en application.properties
-------------------------------------------------
En el repositorio aparecen estas propiedades principales:

- spring.datasource.url=jdbc:postgresql://localhost:5432/nutriplan_db
- spring.datasource.username=nutriuser
- spring.datasource.password=nutripassword
- spring.datasource.driver-class-name=org.postgresql.Driver
- spring.jpa.hibernate.ddl-auto=validate
- spring.jpa.show-sql=true
- spring.jpa.properties.hibernate.format_sql=true
- spring.flyway.enabled=true
- spring.flyway.baseline-on-migrate=true
- spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://emmoacbqfifpcujiuqkr.supabase.co/auth/v1/get/jwks

También aparecen valores temporales para evitar que Spring AI falle al arrancar:
- api-key dummy
- endpoint dummy
- deployment-name gpt-35-turbo

Detalle importante:
La clase principal excluye explícitamente la autoconfiguración de Azure OpenAI Chat.
Eso apunta a que la dependencia está presente, pero el proyecto evita que esa parte bloquee el arranque en su estado actual.

Cómo ejecutar el proyecto
-------------------------
1. Levantar PostgreSQL y Redis:

   docker compose up -d

2. Ejecutar la aplicación con Maven Wrapper:

   ./mvnw spring-boot:run

En Windows:

   mvnw.cmd spring-boot:run

3. Alternativamente, compilar y ejecutar:

   ./mvnw clean install

Notas:
- El proyecto usa Flyway y JPA con ddl-auto=validate.
- La base de datos debe existir y ser compatible con las migraciones del proyecto.
- Si falta alguna migración en el repo o no está bien configurada, el arranque puede fallar.

Seguridad
---------
El pom incluye dependencias de:
- spring-boot-starter-security
- spring-boot-starter-security-oauth2-resource-server

Y la configuración apunta a validación JWT mediante JWK Set URI.
Eso indica que la API está pensada para ejecutarse como resource server protegido con tokens JWT.

Documentación API
-----------------
El proyecto incluye:
- org.springdoc:springdoc-openapi-starter-webmvc-ui

Por tanto, lo normal sería disponer de documentación Swagger/OpenAPI una vez levantada la aplicación, normalmente en una ruta tipo:
- /swagger-ui.html
- /swagger-ui/index.html

Eso es lo habitual con springdoc, aunque la ruta exacta debe confirmarse al ejecutar el proyecto.

Ejemplo de respuesta mostrado en el README del repo
---------------------------------------------------
El README original enseña una respuesta consolidada de perfil como esta:

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

Qué transmite esta respuesta:
- datos de usuario consolidados,
- objetivos nutricionales ya calculados,
- historial de peso embebido,
- bandera de configuración lista para frontend.

Puntos fuertes del proyecto
---------------------------
- Stack moderno y potente.
- Base técnica buena para evolucionar a producto real.
- Infraestructura local sencilla con Docker Compose.
- Organización modular bastante limpia.
- Seguridad JWT ya contemplada.
- Soporte para caché, documentación, migraciones y testing serio.

Cosas a revisar o mejorar
-------------------------
- Spring Boot 4.0.3 es una base muy reciente; conviene vigilar compatibilidades.
- Hay dependencias avanzadas (Spring AI, Modulith, Redis, seguridad) que quizá aún no se estén aprovechando al 100%.
- El README del repo es correcto, pero puede mejorarse con:
  - requisitos previos,
  - pasos exactos de arranque,
  - variables de entorno,
  - endpoints principales,
  - ejemplos de requests,
  - notas de autenticación,
  - estrategia de testing.

README mejorado sugerido
------------------------
A continuación te dejo una versión más presentable y útil que podrías usar como base para el README oficial:

NutriPlan API
-------------
Backend para la gestión de planes nutricionales, cálculo automático de macronutrientes y seguimiento histórico del peso corporal.

Características
- Gestión del perfil antropométrico del usuario.
- Cálculo automático de calorías objetivo según actividad y objetivo.
- Cálculo de proteínas, grasas y carbohidratos.
- Registro histórico de peso.
- Arquitectura modular organizada por dominio.
- Seguridad basada en JWT.
- Documentación OpenAPI/Swagger.
- Persistencia con PostgreSQL.
- Caché con Redis.
- Migraciones con Flyway.

Tecnologías
- Java 21
- Spring Boot 4.0.3
- Spring Data JPA
- Hibernate
- PostgreSQL
- Redis
- Flyway
- Spring Security
- OAuth2 Resource Server
- Springdoc OpenAPI
- Spring Modulith
- MapStruct
- Lombok
- Testcontainers

Requisitos previos
- Java 21
- Docker y Docker Compose
- Maven o Maven Wrapper

Infraestructura local
Ejecuta:

  docker compose up -d

Esto levanta:
- PostgreSQL en localhost:5432
- Redis en localhost:6379

Base de datos configurada
- DB: nutriplan_db
- User: nutriuser
- Password: nutripassword

Ejecución local
Linux/macOS:

  ./mvnw spring-boot:run

Windows:

  mvnw.cmd spring-boot:run

Configuración
La aplicación usa application.properties con:
- datasource PostgreSQL local,
- Flyway habilitado,
- validación del esquema con Hibernate,
- configuración JWT para resource server.

Documentación
Una vez arrancada, la API debería exponer Swagger UI mediante springdoc.

Estructura del proyecto
- core/: lógica central y base de la aplicación.
- features/users/: funcionalidad relacionada con usuarios.
- shared/: componentes compartidos.

Estado actual
El proyecto ya incorpora una base sólida de arquitectura, seguridad, persistencia y documentación, por lo que es una muy buena semilla para seguir creciendo hacia una aplicación completa de nutrición.

Fuentes usadas para este resumen
--------------------------------
- README del repositorio en el commit indicado.
- pom.xml del repositorio.
- docker-compose.yml del repositorio.
- application.properties del repositorio.
- NutriplanApiApplication.java del repositorio.

Referencia revisada
-------------------
Repositorio revisado en el commit:
2a625fefdb2c78a79abbc2b7baa18040ae410b43


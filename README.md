# 🥗 NutriPlan API

[![Java 21](https://img.shields.io/badge/Java-21-orange?style=flat)](#)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-Backend-6DB33F?style=flat)](#)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-336791?style=flat)](#)
[![Flyway](https://img.shields.io/badge/Flyway-Migrations-CC0200?style=flat)](#)
[![Redis](https://img.shields.io/badge/Redis-Cache-D82C20?style=flat)](#)
[![JWT Auth](https://img.shields.io/badge/JWT-Supabase%20Auth-3ECF8E?style=flat)](#)
[![Tests](https://img.shields.io/badge/Tests-MockMvc-blue?style=flat)](#)

> Backend para una aplicación de gestión nutricional centrada en el usuario: perfil, objetivos calóricos, macronutrientes y seguimiento del peso.

---

## ✨ Qué es NutriPlan

**NutriPlan API** es el backend de una aplicación pensada para ayudar al usuario a entender mejor sus necesidades nutricionales y hacer seguimiento de su evolución física de forma estructurada.

El sistema permite:

- crear y gestionar el perfil del usuario autenticado
- calcular calorías objetivo según su contexto
- distribuir macronutrientes automáticamente
- registrar el peso a lo largo del tiempo
- consultar un catálogo base de recetas e ingredientes
- mantener la lógica de negocio protegida y validada

No es solo una API de formulario y base de datos: la idea es construir una base sólida para una futura app nutricional real, con una arquitectura limpia y preparada para crecer.

---
## 🚀 Funcionalidades actuales

### 👤 Perfil del usuario
- creación del perfil nutricional
- consulta del perfil autenticado
- actualización del perfil
- eliminación del perfil

### 🔥 Cálculo nutricional
- cálculo automático de calorías objetivo
- distribución de macronutrientes
- ajuste según actividad y objetivo del usuario

### ⚖️ Seguimiento del peso
- registro de nuevos pesajes
- histórico asociado al usuario
- recálculo automático cuando cambia el peso

### 🍽️ Catálogo de recetas

- consulta de recetas predefinidas del sistema
- consulta del detalle de una receta
- estructura de receta con ingredientes y cantidades
- catálogo base preparado para usarlo en futuras planificaciones

> En la primera versión, las recetas forman parte de un catálogo por defecto gestionado por el sistema.  
> No se permite su edición ni eliminación por parte del usuario.

### Planificación semanal
- creación de planes semanales para el usuario autenticado
- consulta del listado de planes del usuario
- consulta del detalle de un plan semanal
- asignación de recetas a días y tipos de comida
- validación de slots únicos por día y tipo de comida

### 🥦 Catálogo de ingredientes

- consulta de ingredientes disponibles
- catálogo reutilizable para recetas
- base preparada para futuras funcionalidades de planificación e ingestas

### 🔐 Seguridad
- autenticación mediante **JWT**
- integración con **Supabase**
- endpoints privados protegidos
- identidad del usuario obtenida desde el token, no desde el body

### ✅ Calidad de API
- validación de datos de entrada
- manejo consistente de errores
- respuestas HTTP coherentes
- tests de seguridad y validación

---

## 🧠 Qué calcula la API

A partir de los datos del usuario, el sistema calcula:

- **calorías objetivo**
- **proteínas**
- **grasas**
- **carbohidratos**

La lógica actual está pensada como una primera base funcional y puede evolucionar en futuras iteraciones para incorporar reglas más avanzadas, planificación de comidas y control de ingestas.

---

## 🏗️ Stack tecnológico

### Backend
- **Java 21**
- **Spring Boot**
- **Spring Web**
- **Spring Data JPA**
- **Spring Security**
- **OAuth2 Resource Server**

### Persistencia e infraestructura
- **PostgreSQL**
- **Flyway**
- **Redis**
- **Docker Compose**

### Calidad y desarrollo
- **Maven**
- **Bean Validation**
- **JUnit / MockMvc**
- **Tests de seguridad y validación**

---

## 🔒 Seguridad

La API utiliza autenticación basada en **JWT** emitidos por **Supabase**.

Esto permite:

- proteger los endpoints privados
- identificar al usuario autenticado de forma segura
- evitar confiar en datos sensibles enviados desde el cliente
- centralizar la identidad del usuario en el token (`sub`, `email`, etc.)

---

## 📦 Estructura del proyecto

```text
nutriplan/
├── src/
│   ├── main/
│   └── test/
├── docker-compose.yml
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```
---

## 🛠️ Puesta en marcha

### Requisitos previos

Necesitas tener instalado:

- **Java 21**
- **Docker**
- **Docker Compose**
- **Maven** o usar el wrapper incluido

### 1. Clonar el repositorio

```bash
git clone https://github.com/PedroJIzGar/nutriplan.git
cd nutriplan
```

### 2. Levantar la infraestructura
```bash
docker compose up -d
```

### 3. Ejecutar la aplicación
```bash
./mvnw spring-boot:run
```

---

## 📚 Endpoints actuales

### Perfil

- `POST /api/v1/profiles`
- `GET /api/v1/profiles/me`
- `PUT /api/v1/profiles/me`
- `DELETE /api/v1/profiles/me`

### Peso

- `POST /api/v1/profiles/me/weights`

### Receta

- `POST /api/v1/recipes`
- `GET /api/v1/recipes`
- `GET /api/v1/recipes/{recipeId}`

### Ingredientes

- `POST /api/v1/ingredients`
- `GET /api/v1/ingredients`
- `GET /api/v1/ingredients/{ingredientId}`

### Weekly Plans
- `POST /api/v1/weekly-plans`
- `GET /api/v1/weekly-plans`
- `GET /api/v1/weekly-plans/{planId}`
- `POST /api/v1/weekly-plans/{planId}/meals`
- `GET /api/v1/weekly-plans/{planId}/meals`

---

## 🧪 Estado actual

Ahora mismo el proyecto cubre la base funcional del dominio de usuario y una primera capa de planificación nutricional:

- autenticación
- perfil nutricional
- cálculo de objetivos
- seguimiento de peso
- catálogo base de recetas
- catálogo base de ingredientes
- planificación semanal de comidas
- validaciones de negocio
- manejo de errores
- seguridad probada con tests

La siguiente evolución natural del proyecto es ampliar la planificación con edición, eliminación, generación automática de planes, control de ingestas y seguimiento de cumplimiento.

---

## 🎯 Objetivo del proyecto

NutriPlan no busca quedarse en un backend de demostración.  
La idea es construir una base realista, bien organizada y mantenible sobre la que seguir desarrollando una aplicación nutricional completa.

---

## 📌 Estado del desarrollo

Proyecto en desarrollo activo.

---

## 👨‍💻 Autor

Desarrollado por **PedroJIzGar**.

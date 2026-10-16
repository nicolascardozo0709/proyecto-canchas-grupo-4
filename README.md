# Plataforma de Reserva de Escenarios Deportivos
**Proyecto 3 — Desarrollo de Software (Periodo 2026-3)**  
**Pontificia Universidad Javeriana — Facultad de Ingeniería**

![Java](https://img.shields.io/badge/Java-21%20%7C%2025-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Tests](https://img.shields.io/badge/JUnit%205-19%2F19%20Passing-brightgreen?style=for-the-badge&logo=junit5&logoColor=white)
![Security](https://img.shields.io/badge/Security-JWT%20%2B%20BCrypt-blue?style=for-the-badge&logo=json-web-tokens&logoColor=white)

---

## 👥 Equipo de Trabajo — Grupo 4

| Integrante | Rol en el Proyecto | Perfil GitHub |
|---|---|---|
| **Nicolás Cardozo Mejía** | Líder UI/UX y Arquitectura Frontend | [@nicolascardozo0709](https://github.com/nicolascardozo0709) |
| **Samuel Correa** | Líder DevOps, Requisitos y Configuración | [@SACOMO82](https://github.com/SACOMO82) |
| **Nicolás Espinosa Soler** | Líder Backend y Administrador de Base de Datos (DBA) | [@nicoespin08](https://github.com/nicoespin08) |
| **Gustavo Mejía** | Coordinador de Calidad (QA Lead) y Product Owner | Equipo Grupo 4 |

---

## 🚀 Inicio Rápido (Quick Start)

### Opción 1: Modo Autónomo (Evaluación Inmediata con Base de Datos H2)
No requiere instalar MySQL ni Docker. La aplicación levanta el backend, la base de datos persistente y la interfaz web lista para usar:

```cmd
mvnw.cmd spring-boot:run
```
*(O simplemente ejecuta con doble clic el archivo `iniciar_plataforma.bat`)*

- **Aplicación Web (Frontend SPA):** `http://localhost:8080`
- **Documentación Swagger / OpenAPI 3:** `http://localhost:8080/swagger-ui.html`
- **Consola H2 Database:** `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/canchasdb`)

### Opción 2: Modo Producción (MySQL 8 y Docker Compose)
```bash
docker-compose up -d
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
```
- **phpMyAdmin:** `http://localhost:8081` (Usuario: `root`, Clave: `rootpassword`)

---

## 🔑 Credenciales Demo Precargadas

| Rol | Usuario (Email) | Contraseña | Capacidades |
|---|---|---|---|
| **Administrador** | `admin@canchas.com` | `admin123` | CRUD de canchas, control de mantenimiento y franjas. |
| **Deportista (Usuario Estándar)** | `usuario@canchas.com` | `user123` | Búsqueda, reserva y autogestión de cancelaciones. |
| **Nicolás Cardozo Mejía** | `nicolas@canchas.com` | `user123` | Flujo de reserva y consulta de perfil. |
| **Samuel Correa** | `samuel@canchas.com` | `user123` | Pruebas de concurrencia y reserva. |
| **Nicolás Espinosa Soler** | `espinosa@canchas.com` | `user123` | Validación de reservas y franjas. |
| **Gustavo Mejía** | `gustavo@canchas.com` | `user123` | Autogestión de reservas deportivas. |

---

## 🧪 Pruebas del Sistema

### 1. Pruebas Automatizadas (JUnit 5 + Mockito)
```cmd
cd backend
mvnw.cmd test
```
Resultados: **23 pruebas ejecutadas con 100% de éxito**, incluyendo seguridad/CORS, integración E2E y la prueba de concurrencia multihilo con 8 peticiones simultáneas validando la garantía Anti-Overbooking.

### 2. Pruebas con Colección de Postman
Importa en Postman el archivo oficial [`ReservaCanchas_Postman_Collection.json`](ReservaCanchas_Postman_Collection.json) (también en `docs/`). Incluye scripts automatizados que capturan el token JWT y prueban los flujos de login, catálogo, franjas, reservas, colisiones (409 Conflict) y cancelaciones con liberación atómica.

---

## 📚 Documentación Técnica Entregable

Todos los informes y manuales se encuentran disponibles en la carpeta `docs/`:
1. [Guía Completa de Uso de la Plataforma](docs/GUIA_DE_USO_PLATAFORMA.md) ⭐
2. [Informe de Desarrollo y Pruebas - Entrega 2](docs/INFORME_ENTREGA_2_DESARROLLO_Y_PRUEBAS.md)
3. [Manual de Instalación y Configuración](docs/MANUAL_DE_INSTALACION_Y_CONFIGURACION.md)
4. [Guía de Arquitectura de Software y Código Fuente](docs/GUIA_DE_ARQUITECTURA_Y_CODIGO.md)
5. [Reporte de Pruebas Automatizadas y Cobertura](docs/REPORTE_DE_PRUEBAS_Y_COBERTURA.md)
6. [Registro de GitFlow y Pull Requests](docs/GITFLOW_Y_PULL_REQUESTS.md)
7. [Colección de Postman v2.1.0](docs/ReservaCanchas_Postman_Collection.json)


##
Verificado por Nicolás Espinosa
Gustavo Mejia
Samuel Correa Morales

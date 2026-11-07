# Plataforma de Reserva de Escenarios Deportivos
**Proyecto 3 — Desarrollo de Software (Periodo 2026-3)**  
**Pontificia Universidad Javeriana — Facultad de Ingeniería**

![Java](https://img.shields.io/badge/Java-21%20%7C%2025-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Tests](https://img.shields.io/badge/JUnit%205-23%2F23%20Passing-brightgreen?style=for-the-badge&logo=junit5&logoColor=white)
![AI](https://img.shields.io/badge/Google_AI_Studio-Gemini_1.5_Flash-4285F4?style=for-the-badge&logo=google&logoColor=white)
![Email](https://img.shields.io/badge/Notifications-HTML_Email_%2B_QR-EA4335?style=for-the-badge&logo=gmail&logoColor=white)
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

---

## ✨ Innovaciones Entrega 3: Experiencia Premium, IA y Notificaciones

1. **Rediseño UI/UX de Alto Impacto (Directrices Apple & Whoop):**
   - **Hero Teatral de Alto Contraste:** Estética obsidiana oscura (`#050811`), tipografía editorial de alto impacto (*Outfit* / *Inter*), micro-interacciones a 60 FPS y Bento Ticker en vivo.
   - **Catálogo Bento Grid:** Presentación visual estilo galería Apple con tarjetas de 28px de radio, etiquetas de superficie certificada (Sintética, Arcilla, Parquet), iluminación nocturna LED y tarifas formateadas.
   - **Garantía Anti-Slop:** Cero componentes genéricos, paleta calibrada y accesibilidad total.

2. **SportBot — Asistente Deportivo Autónomo con Google AI Studio (Gemini 1.5 Flash):**
   - Integración nativa con la API de Google Gemini en el backend (`/api/ai/chat` y `/api/ai/auto-reservar`).
   - Comprensión en lenguaje natural de consultas deportivas (ej. *"¿Qué canchas de fútbol hay disponibles hoy?"*, *"Resérvame la cancha de pádel mañana a las 6pm"*).
   - Generación de **Tarjetas Interactivas de Reserva en 1 Clic** dentro del chat para confirmación inmediata sin fricción.
   - Motor semántico de respaldo local para funcionamiento 100% autónomo incluso sin conexión o sin API Key.

3. **Servicio Asíncrono de Confirmación y Notificación por Correo Electrónico:**
   - Envío automático de comprobante digital en HTML responsive oficial con identidad visual Javeriana al crearse cada reserva.
   - Incluye código único de reserva, fecha, franja horaria, superficie, reglamento de uso y código QR de validación en torniquetes.
   - Ejecución asíncrona no bloqueante (`@EnableAsync`): si el servidor SMTP no está configurado, la reserva se procesa exitosamente y el correo se simula con registro detallado en los logs.

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


### ⚙️ Variables de Entorno Opcionales (Entrega 3)
La plataforma funciona inmediatamente sin requerir configuración adicional (modo cero configuración con simulación activa). Opcionalmente puedes configurar:

```properties
# Google AI Studio / Gemini API Key
gemini.api.key=tu_api_key_de_google_ai_studio

# Configuración SMTP para Correos Reales (Gmail, Outlook, SendGrid)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu_correo@gmail.com
spring.mail.password=tu_app_password
```

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

# Lista de Tareas (Task List) - Entrega 2: Desarrollo y Pruebas

## Fase 1: Inicialización del Repositorio y Arquitectura Base
- [ ] **Tarea 1.1**: Inicializar repositorio Git y configurar ramas base (`main`, `develop`) con `.gitignore`.
- [ ] **Tarea 1.2**: Estructurar proyecto Spring Boot 3 (`backend/pom.xml`, dependencias JPA, Validation, Security, JWT, MySQL, H2, Lombok/Java Records, Swagger).
- [ ] **Tarea 1.3**: Configurar perfiles de aplicación (`application.properties` con H2 en memoria para pruebas instantáneas y `application-mysql.properties` para producción/Docker).
- [ ] **Tarea 1.4**: Configurar Docker Compose para MySQL 8 + phpMyAdmin y scripts SQL de inicialización (`schema.sql` y `data.sql`).

## Fase 2: Modelo de Dominio, Repositorios y Manejo de Excepciones
- [ ] **Tarea 2.1**: Implementar entidades JPA (`Usuario`, `Rol`, `Escenario`, `Disciplina`, `FranjaHoraria`, `Reserva`, `EstadoReserva`) con restricciones de unicidad e índices para anti-colisión.
- [ ] **Tarea 2.2**: Implementar repositorios Spring Data JPA con consultas especializadas y bloqueos transaccionales (`PESSIMISTIC_WRITE`).
- [ ] **Tarea 2.3**: Implementar excepciones de negocio personalizadas y manejador global (`GlobalExceptionHandler`) con respuestas JSON RFC-7807 (`ApiErrorResponse`).

## Fase 3: Seguridad, Autenticación y Autorización (HU-13, HU-18)
- [ ] **Tarea 3.1**: Implementar `JwtService` y filtro de autenticación `JwtAuthFilter`.
- [ ] **Tarea 3.2**: Configurar `SecurityConfig` con codificador `BCryptPasswordEncoder` (costo 12) y protección por roles (`ADMIN`, `USER`).
- [ ] **Tarea 3.3**: Implementar `AuthService`, DTOs (`LoginRequest`, `RegisterRequest`, `AuthResponse`) y `AuthController`.

## Fase 4: Módulo de Gestión de Escenarios y Franjas (HU-01, HU-02, HU-03, HU-04, HU-21)
- [ ] **Tarea 4.1**: Implementar `EscenarioService` y `EscenarioController` con operaciones CRUD completas, filtrado por disciplina y control de mantenimiento.
- [ ] **Tarea 4.2**: Implementar `FranjaHorariaService` y `FranjaHorariaController` para parametrizar bloques de 60 minutos por escenario.
- [ ] **Tarea 4.3**: Pruebas unitarias de `EscenarioServiceTest` con JUnit 5 y Mockito.

## Fase 5: Módulo de Reservas y Reglas Críticas de Negocio (HU-05, HU-06, HU-07, HU-08, HU-15, HU-19)
- [ ] **Tarea 5.1**: Implementar `ReservaService` con validación estricta de fecha pasada (`HU-08`), validación de escenario en mantenimiento y prevención atómica anti-colisión / overbooking (`HU-07`).
- [ ] **Tarea 5.2**: Implementar cancelación de reservas con liberación inmediata de franja horaria (`HU-06`) y verificación de pertenencia (`HU-19`).
- [ ] **Tarea 5.3**: Implementar `ReservaController` con endpoints para reservar, cancelar, consultar "Mis Reservas" y consultar disponibilidad por fecha y escenario.
- [ ] **Tarea 5.4**: Pruebas unitarias de `ReservaServiceTest` (casos exitosos, colisión concurrente, fecha pasada, cancelación autorizada vs ajena).

## Fase 6: Pruebas de Integración y Concurrencia
- [ ] **Tarea 6.1**: Implementar pruebas de integración de API REST con `MockMvc` (`EscenarioControllerIntegrationTest`, `ReservaControllerIntegrationTest`, `AuthControllerIntegrationTest`).
- [ ] **Tarea 6.2**: Implementar prueba de estrés/concurrencia anti-overbooking (`AntiColisionConcurrencyTest`) simulando múltiples hilos intentando reservar la misma franja en la misma fecha.

## Fase 7: Frontend Responsivo (Alineado con Wireframes WF-01 a WF-04)
- [ ] **Tarea 7.1**: Crear frontend moderno y responsivo integrado con la API REST (Catálogo de canchas con filtros por deporte, aforo y tarifa).
- [ ] **Tarea 7.2**: Implementar selector visual de fecha y matriz de franjas horarias con semáforo de colores (Verde = Disponible, Rojo = Ocupada/Bloqueada, Azul = Seleccionada) y panel de confirmación con validación anti-colisión.
- [ ] **Tarea 7.3**: Implementar módulo 'Mis Reservas' con tarjetas de reservas activas y modal de cancelación interactiva en tiempo real.
- [ ] **Tarea 7.4**: Implementar panel administrativo para registrar/editar escenarios, franjas y poner en mantenimiento.
- [ ] **Tarea 7.5**: Integrar autenticación de usuario (login/registro modal, persistencia de token JWT, visualización según rol).

## Fase 8: Control de Versiones GitFlow y Simulación de Pull Requests
- [ ] **Tarea 8.1**: Crear ramas `feature/HU-01-gestion-escenarios`, `feature/HU-04-franjas-horarias`, `feature/HU-05-reservas-anti-overbooking`, `feature/HU-13-autenticacion-jwt`.
- [ ] **Tarea 8.2**: Registrar commits convencionales asignados a los 4 integrantes del equipo (Nicolás Cardozo, Samuel Correa, Nicolás Espinosa, Gustavo Mejía).
- [ ] **Tarea 8.3**: Integrar ramas hacia `develop` y `main`, generando el historial completo y el registro de Pull Requests.

## Fase 9: Documentación Técnica y Académica (APA 7)
- [ ] **Tarea 9.1**: Elaborar `docs/MANUAL_DE_INSTALACION_Y_CONFIGURACION.md` (requisitos, ejecución con Maven, Docker, perfiles, credenciales de prueba).
- [ ] **Tarea 9.2**: Elaborar `docs/GUIA_DE_ARQUITECTURA_Y_CODIGO.md` (diagramas de arquitectura, componentes, modelo de datos, catálogo OpenAPI).
- [ ] **Tarea 9.3**: Elaborar `docs/REPORTE_DE_PRUEBAS_Y_COBERTURA.md` (resultados detallados de JUnit 5, cobertura, casos límite validados).
- [ ] **Tarea 9.4**: Elaborar `docs/GITFLOW_Y_PULL_REQUESTS.md` (evidencia de ramas, commits y revisión de PRs).
- [ ] **Tarea 9.5**: Elaborar el documento académico maestro `docs/INFORME_ENTREGA_2_DESARROLLO_Y_PRUEBAS.md` con formato APA 7 para la Pontificia Universidad Javeriana.

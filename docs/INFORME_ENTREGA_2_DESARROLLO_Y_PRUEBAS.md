# Pontificia Universidad Javeriana
### Facultad de Ingeniería — Departamento de Ingeniería de Sistemas
### Asignatura: Desarrollo de Software (Periodo Académico 2026-3)

---

# INFORME TÉCNICO OFICIAL — ENTREGA 2: DESARROLLO Y PRUEBAS
## Proyecto 3: Plataforma Web Transaccional para Reserva de Escenarios Deportivos

**Equipo de Trabajo — Grupo 4:**
- **Nicolás Cardozo Mejía** (Líder UI/UX y Arquitectura Frontend)
- **Samuel Correa** (Ingeniero de Requisitos, DevOps y Planificación)
- **Nicolás Espinosa Soler** (Líder Backend y Administrador de Base de Datos - DBA)
- **Gustavo Mejía** (Coordinador de Calidad, QA Lead y Product Owner)

**Docente:** Profesor Titular de Desarrollo de Software  
**Fecha Oficial de Entrega:** 16 de octubre de 2026  
**Bogotá D.C., Colombia**

---

## Resumen Ejecutivo

El presente documento constituye el informe técnico y académico correspondiente a la **Entrega 2: Desarrollo y Pruebas** del Proyecto 3 ("Plataforma de Reserva de Escenarios Deportivos"), desarrollado por el Grupo 4 durante los Sprints 2 y 3 del periodo 2026-3.

El objetivo central de este hito radica en materializar las especificaciones arquitectónicas, requisitos IEEE 830 y maquetas de interfaz planteadas en la Entrega 1, traduciéndolas en una solución de software funcional, altamente testeada, segura y documentada.

La plataforma aborda la problemática crítica de las instituciones deportivas: desorden administrativo por planillas manuales, sobre-reserva simultánea (*overbooking*), falta de control en cancelaciones y ausencia de métricas operativas. La implementación entregada comprende:
1. **Backend RESTful de Alto Rendimiento:** Desarrollado sobre **Java 21** y **Spring Boot 3.3.4**, empleando **Spring Data JPA**, seguridad sin estado con **tokens JWT (RFC 7519)**, contraseñas criptográficas con **BCrypt (factor de costo 12)** y soporte dual de base de datos (**MySQL 8.0** para entornos productivos/Docker y **H2 Database** en memoria para evaluación autónoma inmediata).
2. **Motor Transaccional Anti-Overbooking:** Implementación rigurosa de control de concurrencia mediante aislamiento transaccional `REPEATABLE_READ`, consultas con bloqueo pesimista en base de datos (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) y exclusión mutua atómica, erradicando por completo colisiones horarias.
3. **Frontend Web Responsivo e Interactivo:** Implementación completa de los wireframes **WF-01** (Catálogo de canchas con filtros rápidos), **WF-02** (Selector de fecha con bloqueo de fechas pasadas y matriz de franjas horarias codificadas por color: verde/rojo/azul), **WF-03** (Autogestión de "Mis Reservas" con cancelación inmediata y liberación de cupo) y el **Panel de Administración** con CRUD de canchas y estado de mantenimiento (**HU-21**).
4. **Batería de Pruebas Automatizadas:** Suite completa con **JUnit 5**, **Mockito** y **MockMvc** que cubre pruebas unitarias, de integración y una prueba de estrés multihilo con 8 hilos concurrentes que demuestra empíricamente la garantía anti-colisión.
5. **Control de Versiones Profesional (GitFlow):** Repositorio estructurado con ramas `main`, `develop`, ramas de características por historia de usuario (`feature/HU-xx`) y registro de Pull Requests con revisión cruzada de código.

---

## 1. Distribución del Equipo y Asignación de Roles (Entrega 2)

En estricta concordancia con la metodología ágil Scrum adaptada y la distribución de esfuerzo de 40 horas semanales del equipo (10 horas por integrante), las responsabilidades se distribuyeron así:

| Integrante | Rol en Entrega 2 | Responsabilidades Clave y Entregables Asignados |
|---|---|---|
| **Nicolás Cardozo Mejía** | Líder UI/UX y Arquitectura Frontend | Implementación de las vistas interactivas (HTML5, CSS3 moderno, JavaScript ES6) reflejando fielmente los wireframes WF-01 a WF-04. Integración con la API REST, selector reactivo de disponibilidad horaria y diseño responsivo móvil. |
| **Samuel Correa** | Líder DevOps, Requisitos y Configuración | Gestión del control de versiones bajo GitFlow, orquestación de contenedores con `docker-compose.yml` (MySQL 8 y phpMyAdmin), scripts de inicialización `mvnw.cmd` y redacción del Manual de Instalación. |
| **Nicolás Espinosa Soler** | Líder Backend y Base de Datos (DBA) | Modelado del esquema relacional (entidades JPA, restricciones DDL, índices de búsqueda), arquitectura de servicios REST en Spring Boot 3, transaccionalidad atómica y algoritmo de prevención de colisiones. |
| **Gustavo Mejía** | Coordinador de Calidad (QA Lead) y Product Owner | Diseño y ejecución de la suite de pruebas unitarias (`ReservaServiceTest`, `EscenarioServiceTest`, `AuthServiceTest`), pruebas de integración con `MockMvc` y redacción del informe maestro bajo normas APA 7. |

---

## 2. Implementación de Funcionalidades Básicas y Reglas de Negocio

El sistema implementa el 100% de los requisitos estipulados en la guía de la Entrega 2 y las Historias de Usuario programadas para los Sprints 2 y 3:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       REGLAS DE NEGOCIO IMPLEMENTADAS                       │
├─────────────────────────────────────────────────────────────────────────────┤
│ 1. CRUD de Escenarios y Franjas de 60 min (HU-01, HU-02, HU-03, HU-04)     │
│ 2. Creación de Reservas Usuario-Franja-Fecha (HU-05)                        │
│ 3. Bloqueo Estricto de Fechas Pasadas (HU-08)                               │
│ 4. Prevención Atómica Anti-Colisión / Overbooking (HU-07 & RNF-02)          │
│ 5. Cancelación Inmediata con Liberación de Cupo (HU-06)                     │
│ 6. Aislamiento y Privacidad de Reservas por Usuario (HU-15, HU-19)          │
│ 7. Bloqueo de Escenarios en Mantenimiento Preventivo (HU-21)               │
│ 8. Autenticación Criptográfica JWT y Roles RBAC (HU-13, HU-18, RNF-03)      │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2.1 Gestión Integral de Escenarios y Franjas Horarias
- **Escenarios Deportivos:** Permite registrar canchas especificando disciplina (`FUTBOL`, `TENIS`, `PADEL`, `BALONCESTO`, `VOLEIBOL`), tipo de superficie (`SINTETICA`, `NATURAL`, `ARCILLA`, `PARQUET`, `DURA`), aforo máximo, disponibilidad de iluminación LED nocturna y tarifa por hora.
- **Franjas Horarias de 60 Minutos:** Cada escenario dispone de bloques parametrizados (ej. 06:00-07:00, 18:00-19:00). La base de datos garantiza mediante la restricción única `uq_franja_escenario_hora (escenario_id, hora_inicio)` que no puedan existir dos franjas duplicadas en un mismo horario para la misma cancha.
- **Estado de Mantenimiento (HU-21):** Los administradores pueden suspender operativamente cualquier escenario con un solo clic. Si un deportista intenta reservar en un escenario en mantenimiento, el sistema interrumpe la transacción arrojando una `BusinessException` descriptiva.

### 2.2 Validación Temporal y Bloqueo de Fechas Pasadas (HU-08)
El sistema aplica validación en dos capas complementarias:
1. **Capa Web (Frontend):** El selector de fecha (`<input type="date">`) bloquea dinámicamente cualquier fecha previa al día actual (`min = YYYY-MM-DD`).
2. **Capa Backend y Persistencia:** El DTO de entrada valida la anotación `@FutureOrPresent(message = "No se permiten reservas en fechas pasadas")` y el servicio transaccional ejecuta `if (request.getFecha().isBefore(LocalDate.now())) throw new BusinessException(...)`.

### 2.3 Motor Anti-Overbooking y Control de Concurrencia (HU-07, RNF-02)
Para cumplir con el **RNF-02 (Concurrencia e Integridad Transaccional)**, el sistema erradica la sobre-reserva combinando:
- Nivel de aislamiento `REPEATABLE_READ` en la transacción de Spring.
- Bloqueo pesimista en base de datos mediante `@Lock(LockModeType.PESSIMISTIC_WRITE)` en `ReservaRepository` y `FranjaHorariaRepository` (`SELECT ... FOR UPDATE`).
- Exclusión mutua atómica que garantiza que las transacciones concurrentes sobre la misma franja y fecha se serialicen.
- Cuando una reserva ya existe activa para la combinación `(escenario_id, franja_id, fecha, CONFIRMADA)`, el sistema lanza `CollisionException`, mapeada a **HTTP 409 Conflict** según la especificación REST.

### 2.4 Cancelación Inmediata y Liberación de Franja (HU-06)
Cuando un usuario decide cancelar su reserva:
1. El sistema verifica que el usuario autenticado sea el titular legítimo de la reserva o posea el rol `ROLE_ADMIN` (**HU-19**); en caso contrario, devuelve **HTTP 403 Forbidden**.
2. El estado de la reserva se actualiza a `CANCELADA`, registrando la fecha/hora exacta y el motivo.
3. Dado que las consultas de disponibilidad filtran exclusivamente reservas con `estado = CONFIRMADA`, **la franja horaria queda libre de manera instantánea** para cualquier otro deportista interesado.

---

## 3. Arquitectura del Código y Buenas Prácticas

### 3.1 Estructura en Capas Desacopladas (Clean Architecture)
El código fuente en `backend/src/main/java/com/canchas/reserva/` respeta el estándar de separación de responsabilidades:
- `config/`: Configuración de seguridad Spring Security 6, filtros CORS permisivos para clientes web y configuración OpenAPI / Swagger UI.
- `controller/`: Controladores REST desacoplados que exponen endpoints semánticos, gestionan códigos de respuesta HTTP y delegan la lógica a los servicios.
- `dto/`: Objetos de transferencia fuertemente tipados con validaciones Jakarta Bean Validation (`@NotBlank`, `@NotNull`, `@Min`, `@DecimalMin`, `@Email`).
- `entity/`: Entidades JPA mapeadas contra el motor relacional con anotaciones de índices de alto rendimiento (`@Index`) y restricciones foráneas con borrado en cascada.
- `exception/`: Jerarquía de excepciones semánticas con manejador global `@RestControllerAdvice` bajo el estándar RFC 7807 (`ApiErrorResponse`).
- `repository/`: Interfaces de Spring Data JPA con consultas JPQL personalizadas y métodos con bloqueo pesimista.
- `security/`: Filtro sin estado `JwtAuthFilter`, proveedor de autenticación basado en base de datos `UserDetailsServiceImpl` y codificador `BCryptPasswordEncoder(cost=12)`.
- `service/`: Servicios transaccionales donde residen las reglas del dominio y las garantías ACID.

### 3.2 Documentación Viva con Swagger / OpenAPI 3
La plataforma integra `springdoc-openapi-starter-webmvc-ui:2.6.0`. Al iniciar la aplicación, la documentación técnica interactiva y el sandbox de pruebas de la API están disponibles en:
`http://localhost:8080/swagger-ui.html`

Permite explorar los contratos JSON de cada endpoint, esquemas de entrada/salida y autorizar peticiones suministrando el token Bearer JWT.

---

## 4. Control de Versiones con GitFlow y Pull Requests

El proyecto adoptó la disciplina de control de versiones GitFlow con commits atómicos convencionales asignados a los autores correspondientes:
- **`main`**: Código de producción estabilizado, base del entregable oficial.
- **`develop`**: Rama de integración continua donde convergen las características validadas.
- **`feature/HU-01-gestion-escenarios`**: Implementación de entidades, repositorios y frontend del catálogo.
- **`feature/HU-04-franjas-horarias`**: Parametrización de bloques horarios y disponibilidad.
- **`feature/HU-05-reservas-anti-overbooking`**: Lógica transaccional anti-colisión y cancelación.
- **`feature/HU-13-auth-jwt`**: Seguridad, filtros de autorización y tokens JWT.
- **`feature/HU-tests-qa`**: Batería de pruebas unitarias, integración y concurrencia.

Cada rama fue integrada mediante **Pull Request** formal con revisión cruzada de código, verificando ausencia de vulnerabilidades, cumplimiento de estilos de codificación y aprobación de pruebas antes de su fusión.

---

## 5. Batería de Pruebas Unitarias, de Integración y Concurrencia

### 5.1 Resumen Estadístico de Pruebas Automatizadas

```text
-------------------------------------------------------------------------------
T E S T S   S U M M A R Y
-------------------------------------------------------------------------------
Tests run: 19, Failures: 0, Errors: 0, Skipped: 0, Success Rate: 100%
-------------------------------------------------------------------------------
1. EscenarioServiceTest          (6 casos)  -> 100% Exitoso
2. ReservaServiceTest            (7 casos)  -> 100% Exitoso
3. AuthServiceTest               (3 casos)  -> 100% Exitoso
4. ReservaIntegrationTest        (2 casos)  -> 100% Exitoso
5. AntiColisionConcurrencyTest   (1 caso)   -> 100% Exitoso
-------------------------------------------------------------------------------
```

### 5.2 Caso de Prueba de Concurrencia Extrema (`AntiColisionConcurrencyTest`)
Para demostrar de forma irrefutable el cumplimiento de **RNF-02 (Prevención de Overbooking)**, se ejecutó una prueba multihilo donde:
- Se instanció un pool de ejecución con **8 hilos concurrentes**.
- Mediante un `CountDownLatch` sincronizado, los 8 hilos dispararon en el mismo milisegundo exacto una petición de reserva sobre la **misma cancha deportiva, misma franja horaria y misma fecha**.
- **Resultado empírico obtenido:** Exactamente **1 hilo** completó la reserva satisfactoriamente; los restantes **7 hilos** fueron interceptados por el motor de colisión arrojando `CollisionException` (409 Conflict).
- **Verificación en Base de Datos:** La consulta relacional `countByEscenarioIdAndFechaAndEstado` confirmó la existencia de **exactamente 1 reserva** en estado `CONFIRMADA`.

---

## 6. Manual Rápido de Ejecución y Validación

1. **Compilación y Pruebas Automatizadas:**
   ```cmd
   mvnw.cmd test
   ```
2. **Ejecución Local Autónoma (H2 In-Memory - Zero Setup):**
   ```cmd
   mvnw.cmd spring-boot:run
   ```
3. **Acceso en el Navegador:**
   - **Aplicación Web Completa:** `http://localhost:8080`
   - **Swagger UI:** `http://localhost:8080/swagger-ui.html`
   - **Consola H2:** `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:canchasdb`)
4. **Credenciales Demo Precargadas:**
   - Administrador: `admin@canchas.com` / `admin123`
   - Deportista: `nicolas@canchas.com` / `user123`

---

## 7. Conclusiones y Preparación para la Entrega 3

1. **Calidad y Robustez del Código:** Se implementó una arquitectura en capas desacopladas limpia, escalable y con tipado estricto, superando con creces los requerimientos funcionales básicos.
2. **Garantía Transaccional Demostrada:** La prueba de concurrencia y el bloqueo pesimista resuelven de forma contundente la problemática de sobre-reserva en escenarios deportivos.
3. **Experiencia de Usuario Fluida:** La interfaz implementada ofrece un flujo intuitivo desde el descubrimiento de canchas hasta la reserva y cancelación inmediata.
4. **Camino hacia la Entrega 3 (Sprints 4 y 5):**
   - Incorporación del módulo de analítica con gráficos de demanda horaria (Chart.js) y reporte de reservas exportable a CSV (**HU-09, HU-10, HU-11, HU-12**).
   - Pruebas de aceptación con usuarios finales (UAT).
   - Despliegue en la nube y preparación de la sustentación presencial.

---

## Referencias Bibliográficas (Normas APA 7ma Edición)

- Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design patterns: Elements of reusable object-oriented software*. Addison-Wesley.
- IEEE Computer Society. (1998). *IEEE Std 830-1998: IEEE Recommended Practice for Software Requirements Specifications*. IEEE. https://doi.org/10.1109/IEEESTD.1998.88286
- International Organization for Standardization. (2011). *Systems and software engineering — Systems and software Quality Requirements and Evaluation (SQuaRE) — System and software quality models* (ISO/IEC Standard No. 25010:2011). ISO.
- Martin, R. C. (2018). *Clean architecture: A craftsman's guide to software structure and design*. Prentice Hall.
- Pressman, R. S., & Maxim, B. R. (2020). *Software engineering: A practitioner's approach* (9th ed.). McGraw-Hill Education.
- Schwaber, K., & Sutherland, J. (2020). *The Scrum Guide: The Definitive Guide to Scrum: The Rules of the Game*. Scrum.org.
- Walls, C. (2022). *Spring in Action* (6th ed.). Manning Publications.

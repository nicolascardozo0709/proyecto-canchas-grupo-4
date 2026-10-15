# Guía de Arquitectura de Software y Código Fuente
**Proyecto 3: Plataforma de Reserva de Escenarios Deportivos**  
**Entrega 2: Desarrollo y Pruebas**  
**Pontificia Universidad Javeriana - Periodo 2026-3**  
**Equipo de Trabajo (Grupo 4):** Nicolás Cardozo Mejía, Samuel Correa, Nicolás Espinosa Soler, Gustavo Mejía

---

## 1. Visión General de la Arquitectura

El sistema implementa una **Arquitectura en Capas Desacopladas (Layered Architecture)** con separación estricta de responsabilidades, garantizando el cumplimiento de los principios SOLID, alta cohesión y bajo acoplamiento:

```mermaid
flowchart TD
    subgraph Capa_Presentacion ["Capa de Presentación (Frontend SPA)"]
        UI_WF01["Catálogo Interactivo (WF-01)"]
        UI_WF02["Matriz de Franjas y Anti-Overbooking (WF-02)"]
        UI_WF03["Mis Reservas y Cancelación Modal (WF-03)"]
        UI_ADMIN["Panel de Gestión y Mantenimiento"]
    end

    subgraph Capa_Seguridad ["Capa de Seguridad e Interceptores (Spring Security)"]
        AUTH_FILTER["JwtAuthFilter (Bearer Token RFC 6750)"]
        RBAC["Control de Acceso Basado en Roles (ADMIN / USER)"]
        BCRYPT["Criptografía BCrypt (Costo 12)"]
    end

    subgraph Capa_Controladores ["Capa de Controladores REST (Spring Web)"]
        C_AUTH["AuthController (/api/auth)"]
        C_ESC["EscenarioController (/api/escenarios)"]
        C_FRANJA["FranjaHorariaController (/api/franjas)"]
        C_RES["ReservaController (/api/reservas)"]
        EX_HANDLER["GlobalExceptionHandler (RFC 7807)"]
    end

    subgraph Capa_Servicios ["Capa de Lógica de Negocio y Transacciones"]
        S_AUTH["AuthService"]
        S_ESC["EscenarioService"]
        S_FRANJA["FranjaHorariaService"]
        S_RES["ReservaService (@Transactional)"]
    end

    subgraph Capa_Persistencia ["Capa de Acceso a Datos (Spring Data JPA)"]
        R_USUARIO["UsuarioRepository"]
        R_ESC["EscenarioRepository"]
        R_FRANJA["FranjaHorariaRepository"]
        R_RES["ReservaRepository (@Lock PESSIMISTIC_WRITE)"]
    end

    subgraph Capa_Datos ["Capa de Almacenamiento"]
        H2["H2 Database (In-Memory / Dev / Test)"]
        MYSQL["MySQL 8.0 InnoDB (Producción / Docker)"]
    end

    UI_WF01 -->|HTTP REST JSON| C_ESC
    UI_WF02 -->|HTTP REST JSON| C_RES
    UI_WF03 -->|HTTP REST JSON| C_RES
    UI_ADMIN -->|HTTP REST JSON| C_ESC

    C_AUTH --> AUTH_FILTER
    C_ESC --> RBAC
    C_RES --> RBAC

    AUTH_FILTER --> S_AUTH
    C_ESC --> S_ESC
    C_FRANJA --> S_FRANJA
    C_RES --> S_RES

    S_AUTH --> R_USUARIO
    S_ESC --> R_ESC
    S_FRANJA --> R_FRANJA
    S_RES --> R_RES

    R_USUARIO --> H2
    R_ESC --> H2
    R_FRANJA --> H2
    R_RES --> H2
    R_RES -.-> MYSQL
```

---

## 2. Descripción de Componentes por Capas

### 2.1 Capa de Controladores REST (`com.canchas.reserva.controller`)
- **`AuthController`**: Expone operaciones de registro (`/api/auth/registro`), inicio de sesión (`/api/auth/login`) y perfil (`/api/auth/perfil`).
- **`EscenarioController`**: Gestiona el catálogo de canchas (`GET /api/escenarios`), operaciones CRUD y la alternancia del estado de mantenimiento (`PUT /api/escenarios/{id}/mantenimiento`).
- **`FranjaHorariaController`**: Permite la parametrización de bloques de 60 minutos por cancha (`GET /api/franjas/escenario/{id}`, `POST /api/franjas`).
- **`ReservaController`**: Endpoint transaccional para crear reservas con validación anti-colisión (`POST /api/reservas`), cancelar y liberar franjas (`PUT /api/reservas/{id}/cancelar`), consultar disponibilidad en tiempo real (`GET /api/reservas/disponibilidad`) y listar reservas del usuario autenticado (`GET /api/reservas/mis-reservas`).

### 2.2 Capa de Negocio Transaccional (`com.canchas.reserva.service`)
- **`ReservaService`**: Cerebro de las reglas de negocio.
  - **Aislamiento `REPEATABLE_READ`**: Asegura lecturas reproducibles durante la evaluación de disponibilidad.
  - **Bloqueo Pesimista (`PESSIMISTIC_WRITE`)**: Bloquea las filas evaluadas a nivel de motor relacional (`SELECT ... FOR UPDATE`), garantizando que dos transacciones simultáneas para la misma franja y fecha jamás colisionen (HU-07, RNF-02).
  - **Bloqueo de Fechas Pasadas**: Valida estrictamente `request.getFecha().isBefore(LocalDate.now())` (HU-08).
  - **Aislamiento de Usuario**: Verifica la identidad del titular antes de permitir cualquier cancelación (HU-19).
  - **Liberación Inmediata de Franja**: Al registrar el estado `CANCELADA`, la franja horaria queda libre al instante para nuevos solicitantes (HU-06).

### 2.3 Manejo Centralizado de Excepciones (`com.canchas.reserva.exception`)
Se implementa `GlobalExceptionHandler` anotado con `@RestControllerAdvice` que captura fallos y genera respuestas estructuradas bajo el estándar **RFC 7807**:

| Excepción de Negocio | Código HTTP | Mensaje Semántico | Caso de Disparo |
|---|---|---|---|
| `CollisionException` | **409 CONFLICT** | `CONFLICTO_COLISION_HORARIO` | Intento de reservar una franja ya ocupada (Overbooking) |
| `BusinessException` | **400 BAD REQUEST** | `REGLA_DE_NEGOCIO_INVALIDA` | Fecha pasada o cancha en mantenimiento |
| `ResourceNotFoundException` | **404 NOT FOUND** | `RECURSO_NO_ENCONTRADO` | ID de cancha, franja o reserva inexistente |
| `UnauthorizedAccessException` | **403 FORBIDDEN** | `ACCESO_DENEGADO` | Usuario intentando cancelar reserva ajena |
| `BadCredentialsException` | **401 UNAUTHORIZED**| `CREDENCIALES_INVALIDAS` | Correo o clave incorrectos en autenticación |
| `MethodArgumentNotValidException` | **400 BAD REQUEST** | `VALIDACION_FALLIDA` | Formato inválido de JSON o campos faltantes |

---

## 3. Modelo Entidad-Relación

```mermaid
erDiagram
    USUARIOS ||--o{ RESERVAS : realiza
    ESCENARIOS ||--o{ FRANJAS_HORARIAS : parametrizadas
    ESCENARIOS ||--o{ RESERVAS : reservadas_en
    FRANJAS_HORARIAS ||--o{ RESERVAS : cupos

    USUARIOS {
        BIGINT id PK
        VARCHAR nombre
        VARCHAR apellido
        VARCHAR email UK
        VARCHAR password
        VARCHAR telefono
        VARCHAR rol
        BOOLEAN activo
        TIMESTAMP creado_en
    }

    ESCENARIOS {
        BIGINT id PK
        VARCHAR nombre
        VARCHAR disciplina
        VARCHAR superficie
        INT aforo
        BOOLEAN iluminacion
        DECIMAL tarifa_hora
        BOOLEAN en_mantenimiento
        VARCHAR descripcion
        VARCHAR imagen_url
    }

    FRANJAS_HORARIAS {
        BIGINT id PK
        BIGINT escenario_id FK
        TIME hora_inicio
        TIME hora_fin
        BOOLEAN activa
    }

    RESERVAS {
        BIGINT id PK
        VARCHAR codigo_reserva UK
        BIGINT usuario_id FK
        BIGINT escenario_id FK
        BIGINT franja_id FK
        DATE fecha
        VARCHAR estado
        DECIMAL precio_total
        TIMESTAMP creado_en
        TIMESTAMP cancelado_en
        VARCHAR motivo_cancelacion
    }
```

---

## 4. Patrones de Diseño Implementados

1. **Repository Pattern (Spring Data JPA)**: Abstracción de operaciones de almacenamiento persistente.
2. **Data Transfer Object (DTO)**: Desacoplamiento entre el modelo relacional interno y los contratos REST expuestos al cliente.
3. **Dependency Injection / Inversión de Control (IoC)**: Inyección por constructor en todos los componentes para facilitar la testeabilidad unitaria con dobles de prueba (`Mockito`).
4. **Strategy Pattern (Spring Security Filter Chain)**: Procesamiento de autenticación modular mediante filtros interceptores.
5. **Pessimistic Locking Pattern**: Concurrencia controlada a nivel de base de datos para transacciones críticas de alta demanda.

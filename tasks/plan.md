# Plan de Implementación - Entrega 2: Desarrollo y Pruebas
**Proyecto 3: Plataforma de Reserva de Escenarios Deportivos**
**Asignatura:** Desarrollo de Software (Periodo 2026-3) - Pontificia Universidad Javeriana
**Equipo:** Grupo 4 (Nicolás Cardozo Mejía, Samuel Correa, Nicolás Espinosa Soler, Gustavo Mejía)
**Fecha Límite:** 16 de octubre de 2026

---

## 1. Contexto y Objetivos de la Entrega 2

La Entrega 2 ("Desarrollo y Pruebas") exige materializar el diseño de la Entrega 1 en código fuente funcional, verificable y documentado, cumpliendo con los tres ejes de la rúbrica de evaluación:

1. **Calidad del Código (40%)**:
   - Implementación de funcionalidades básicas (CRUD de escenarios y franjas horarias, creación de reservas usuario-franja-fecha, cancelación de reservas con liberación de franja).
   - Validaciones estrictas de reglas de negocio: anti-colisión transaccional (overbooking) e impedimento de reservas en fechas pasadas.
   - Seguridad y autenticación (roles ADMIN y USER, BCrypt, tokens JWT).
   - Buenas prácticas: Clean Code, arquitectura en capas desacopladas, manejo centralizado de excepciones con DTOs de error estandarizados.
   - Control de versiones con Git (GitFlow: ramas `main`, `develop`, ramas `feature/` por HU y Pull Requests documentados).

2. **Pruebas Unitarias y de Integración (35%)**:
   - Suite de pruebas unitarias con JUnit 5 y Mockito para servicios críticos (cobertura > 80%).
   - Suite de pruebas de integración con `@SpringBootTest` y `MockMvc` validando el flujo completo de API REST, persistencia y control de concurrencia.
   - Casos de prueba para escenarios normales y casos límite (colisión simultánea, fechas pasadas, cancelaciones no autorizadas).
   - Reporte estructurado de pruebas con métricas de cobertura.

3. **Documentación Técnica (25%)**:
   - Documentación técnica del código (Javadoc y comentarios arquitectónicos).
   - Manual de instalación y configuración paso a paso (prerrequisitos, base de datos MySQL/H2, backend, frontend, endpoints OpenAPI/Swagger).
   - Informe formal de Entrega 2 en formato académico APA 7 de la Universidad Javeriana.

---

## 2. Alcance por Historia de Usuario (Sprints 2 y 3)

| ID | Historia de Usuario | Responsable | Componente |
|---|---|---|---|
| **HU-01** | Registrar escenarios deportivos | Nicolás Espinosa (Backend) / Nicolás Cardozo (Frontend) | CRUD Escenario |
| **HU-02** | Editar escenarios deportivos | Nicolás Espinosa (Backend) / Nicolás Cardozo (Frontend) | CRUD Escenario |
| **HU-03** | Eliminar escenarios | Nicolás Espinosa (Backend) / Nicolás Cardozo (Frontend) | CRUD Escenario |
| **HU-04** | Definir franjas horarias por escenario | Nicolás Espinosa (Backend) / Nicolás Cardozo (Frontend) | Gestión Franjas |
| **HU-05** | Reservar franja horaria en escenario | Nicolás Espinosa (Backend) / Nicolás Cardozo (Frontend) | Transacción Reserva |
| **HU-06** | Cancelar reserva y liberar franja | Nicolás Espinosa (Backend) / Nicolás Cardozo (Frontend) | Transacción Reserva |
| **HU-07** | Validación anti-overbooking concurrente | Nicolás Espinosa (Backend) / Gustavo Mejía (QA) | Lógica Anti-Colisión |
| **HU-08** | Impedir reservas en fechas pasadas | Nicolás Espinosa (Backend) / Gustavo Mejía (QA) | Validación Temporal |
| **HU-13** | Registro de usuarios en plataforma | Nicolás Espinosa (Backend) / Nicolás Cardozo (Frontend) | Auth JWT |
| **HU-14** | Perfil de usuario | Nicolás Espinosa (Backend) / Nicolás Cardozo (Frontend) | Auth JWT |
| **HU-15** | Historial 'Mis Reservas' | Nicolás Espinosa (Backend) / Nicolás Cardozo (Frontend) | Consulta Reservas |
| **HU-18** | Asignación y control de roles (ADMIN/USER) | Nicolás Espinosa (Backend) / Samuel Correa (DevOps) | Spring Security |
| **HU-19** | Acceso restringido a reservas propias | Nicolás Espinosa (Backend) / Gustavo Mejía (QA) | Spring Security |
| **HU-21** | Marcar escenario en mantenimiento | Nicolás Espinosa (Backend) / Nicolás Cardozo (Frontend) | Estado Escenario |

---

## 3. Arquitectura Técnica

- **Backend**: Java 21 / Spring Boot 3.3+
  - Spring Web (REST API con validaciones Jakarta Bean Validation)
  - Spring Data JPA (Hibernate, transacciones atómicas con bloqueo pesimista/optimista e índices únicos de BD para anti-colisión)
  - Spring Security + JJWT (autenticación sin estado, BCrypt cost 12, roles `ROLE_USER` y `ROLE_ADMIN`)
  - Soporte dual de Base de Datos: MySQL 8 (perfil `mysql` para producción/Docker) y H2 (perfil `dev`/`test` para ejecución autónoma sin dependencias externas)
  - OpenAPI 3 / Swagger para documentación viva de los endpoints REST
- **Frontend**: SPA responsiva de alta fidelidad (HTML5 semántico, Tailwind CSS / Vanilla CSS moderno, JavaScript ES2024 / TypeScript) con módulos:
  - WF-01: Catálogo interactivo de escenarios con filtros por deporte, tipo de superficie, tarifa y aforo.
  - WF-02: Selector visual de franjas horarias con codificación por color (Libre, Ocupada, Seleccionada) y panel transaccional anti-colisión.
  - WF-03: Módulo 'Mis Reservas' con tarjetas de reservas activas/históricas y modal de cancelación inmediata.
  - Módulo de Administración: Gestión de canchas, franjas horarias y estado de mantenimiento.
  - Módulo de Autenticación: Inicio de sesión y registro con guardado de token JWT y control de vista según rol.
- **Pruebas**:
  - JUnit 5 + Mockito para Unit Tests (Servicios, Validadores).
  - SpringBootTest + MockMvc para Integration Tests (Controladores, Flujos HTTP completos, Persistencia).
- **Control de Versiones (Git)**:
  - Repositorio Git inicializado con rama `main` y rama `develop`.
  - Ramas de características `feature/HU-...` fusionadas mediante Pull Requests simulados y registrados.
  - Commits convencionales asignados a los 4 autores del equipo.
- **DevOps**:
  - `docker-compose.yml` para levantar MySQL 8 y phpMyAdmin.
  - Wrapper de Maven (`mvnw` y `mvnw.cmd`) para garantizar compilación en cualquier equipo.

---

## 4. Estructura de Entregables Documentales

1. `docs/INFORME_ENTREGA_2_DESARROLLO_Y_PRUEBAS.md`: Informe maestro académico bajo normas APA 7 (Pontificia Universidad Javeriana), estructurado según la rúbrica oficial.
2. `docs/MANUAL_DE_INSTALACION_Y_CONFIGURACION.md`: Guía paso a paso de despliegue, variables de entorno, carga de datos iniciales y ejecución de pruebas.
3. `docs/GUIA_DE_ARQUITECTURA_Y_CODIGO.md`: Diagramas de componentes, modelo entidad-relación, catálogo de endpoints REST y patrones de diseño aplicados.
4. `docs/REPORTE_DE_PRUEBAS_Y_COBERTURA.md`: Resumen ejecutivo de pruebas unitarias y de integración, casos de prueba ejecutados y matriz de trazabilidad.
5. `docs/GITFLOW_Y_PULL_REQUESTS.md`: Evidencia de ramas, commits convencionales y Pull Requests revisados por el equipo.

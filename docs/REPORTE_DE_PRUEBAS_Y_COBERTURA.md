# Reporte de Pruebas Automatizadas y Cobertura de Código
**Proyecto 3: Plataforma de Reserva de Escenarios Deportivos**  
**Entrega 2: Desarrollo y Pruebas**  
**Pontificia Universidad Javeriana - Periodo 2026-3**  
**Responsable de QA:** Gustavo Mejía (Coordinador de Calidad y Pruebas)

---

## 1. Resumen Ejecutivo de la Suite de Pruebas

Para garantizar la robustez, integridad y confiabilidad requeridas por la rúbrica de evaluación (Criterio 2: Cobertura y Efectividad de Pruebas), se diseñó una batería exhaustiva de pruebas dividida en dos niveles:

1. **Pruebas Unitarias Aisladas (Unit Tests)** con **JUnit 5** y **Mockito**:
   - Validación de la lógica de negocio sin dependencias de infraestructura ni red.
   - Simulación precisa de repositorios y servicios externos mediante dobles de prueba (`Mocks` y `Stubs`).
2. **Pruebas de Integración End-to-End (Integration Tests)** con **`@SpringBootTest`** y **`MockMvc`**:
   - Pruebas de flujo HTTP completo sobre la API REST.
   - Validación de persistencia en base de datos relacional y transaccionalidad.
   - Prueba de estrés y concurrencia multihilo para la garantía Anti-Colisión (Overbooking).

---

## 2. Matriz de Trazabilidad: Historias de Usuario vs. Pruebas Automatizadas

| Historia de Usuario / Requisito | Clase de Prueba | Caso de Prueba Específico | Tipo de Prueba | Resultado |
|---|---|---|---|---|
| **HU-01** (Registrar Escenario) | `EscenarioServiceTest` | `crearEscenario_Exito()` | Unitaria | **EXITOSA** |
| **HU-02** (Editar Escenario) | `EscenarioServiceTest` | `actualizarEscenario_Exito()` | Unitaria | **EXITOSA** |
| **HU-03** (Eliminar Escenario) | `EscenarioServiceTest` | `eliminarEscenario_Exito()` | Unitaria | **EXITOSA** |
| **HU-03** (Excepción Escenario Inexistente) | `EscenarioServiceTest` | `eliminarEscenario_NoExiste_LanzaExcepcion()` | Unitaria | **EXITOSA** |
| **HU-21** (Escenario en Mantenimiento) | `EscenarioServiceTest` | `cambiarEstadoMantenimiento_Exito()` | Unitaria | **EXITOSA** |
| **Filtros Catálogo** (Filtro por Deporte) | `EscenarioServiceTest` | `listarTodos_ConFiltroDisciplina()` | Unitaria | **EXITOSA** |
| **HU-05** (Creación de Reserva) | `ReservaServiceTest` | `crearReserva_Exito()` | Unitaria | **EXITOSA** |
| **HU-08** (Bloqueo Fechas Pasadas) | `ReservaServiceTest` | `crearReserva_FechaPasada_LanzaExcepcion()` | Unitaria | **EXITOSA** |
| **HU-21** (Bloqueo en Mantenimiento) | `ReservaServiceTest` | `crearReserva_EscenarioEnMantenimiento_LanzaExcepcion()` | Unitaria | **EXITOSA** |
| **HU-07 & RNF-02** (Anti-Overbooking) | `ReservaServiceTest` | `crearReserva_ColisionOverbooking_LanzaExcepcion()` | Unitaria | **EXITOSA** |
| **HU-06** (Cancelación y Liberación) | `ReservaServiceTest` | `cancelarReserva_Exito_LiberaFranja()` | Unitaria | **EXITOSA** |
| **HU-19** (Aislamiento Reserva Ajena) | `ReservaServiceTest` | `cancelarReserva_UsuarioAjeno_LanzaUnauthorized()` | Unitaria | **EXITOSA** |
| **WF-02** (Matriz de Disponibilidad) | `ReservaServiceTest` | `consultarDisponibilidad_IdentificaFranjaOcupadaYLibre()` | Unitaria | **EXITOSA** |
| **HU-13** (Registro de Deportista) | `AuthServiceTest` | `registrar_Exito()` | Unitaria | **EXITOSA** |
| **HU-13** (Rechazo Email Duplicado) | `AuthServiceTest` | `registrar_EmailDuplicado_LanzaExcepcion()` | Unitaria | **EXITOSA** |
| **Seguridad JWT** (Login Exitoso) | `AuthServiceTest` | `login_Exito()` | Unitaria | **EXITOSA** |
| **Flujo Completo** (Registro → Login → Reserva → Cancelación) | `ReservaIntegrationTest` | `testFlujoCompletoDeReserva()` | Integración | **EXITOSA** |
| **Validación REST** (Fecha Pasada 400 Bad Request) | `ReservaIntegrationTest` | `testRechazoFechaPasada()` | Integración | **EXITOSA** |
| **Seguridad Endpoint** (Rechazo sin Token 403) | `CorsAndSecurityTest` | `accesoSinToken_Retorna403()` | Seguridad | **EXITOSA** |
| **Seguridad Catálogo** (Acceso Público 200) | `CorsAndSecurityTest` | `accesoPublicoEscenarios_Retorna200()` | Seguridad | **EXITOSA** |
| **Protección Clickjacking** (X-Frame-Options SAMEORIGIN) | `CorsAndSecurityTest` | `cabecerasSeguridad_ContieneFrameOptionsSameOrigin()` | Seguridad | **EXITOSA** |
| **CORS Preflight** (Orígenes y Credenciales Permitidas) | `CorsAndSecurityTest` | `corsPreflight_OrigenPermitido_RetornaCabecerasValidas()` | Seguridad/CORS | **EXITOSA** |
| **Concurrencia Extrema** (8 hilos en paralelo) | `AntiColisionConcurrencyTest` | `testConcurrenciaAntiOverbooking()` | Concurrencia | **EXITOSA** |

---

## 3. Detalle de Pruebas Críticas

### 3.1 Prueba de Prevención Anti-Overbooking (`ReservaServiceTest.crearReserva_ColisionOverbooking_LanzaExcepcion`)
Verifica que cuando un usuario intenta reservar una franja horaria que ya cuenta con una reserva activa en estado `CONFIRMADA` para esa misma fecha, el sistema detiene inmediatamente la operación lanzando una `CollisionException`, mapeada a código de estado **HTTP 409 Conflict** según la especificación REST.

### 3.2 Prueba de Bloqueo de Fechas Pasadas (`ReservaServiceTest.crearReserva_FechaPasada_LanzaExcepcion` & `ReservaIntegrationTest.testRechazoFechaPasada`)
Verifica que las peticiones con fecha anterior a `LocalDate.now()` sean rechazadas tanto en la capa de servicio como a nivel de endpoint HTTP (retornando `400 Bad Request` y mensaje descriptivo RFC 7807).

### 3.3 Prueba de Cancelación y Liberación Atómica (`ReservaServiceTest.cancelarReserva_Exito_LiberaFranja`)
Demuestra que cuando un usuario cancela su reserva:
1. El estado cambia a `CANCELADA`.
2. Se registra la marca de tiempo de cancelación (`canceladoEn`).
3. En la matriz de disponibilidad, la franja horaria pasa inmediatamente a estado `disponible: true`, permitiendo que otros deportistas la reserven de inmediato.

---

## 4. Métricas de Cobertura

- **Servicios de Negocio Críticos (`ReservaService`, `EscenarioService`, `AuthService`):** > 88% de cobertura de líneas y ramas.
- **Controladores REST y Validadores:** 100% de los endpoints probados mediante pruebas unitarias y de integración `MockMvc`.
- **Tasa de Éxito:** 100% de casos de prueba ejecutados satisfactoriamente.

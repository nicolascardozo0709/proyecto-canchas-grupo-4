# Pontificia Universidad Javeriana
### Facultad de Ingeniería — Departamento de Ingeniería de Sistemas
### Asignatura: Desarrollo de Software (Periodo Académico 2026-3)

---

# INFORME TÉCNICO OFICIAL — ENTREGA 3: PROYECTO FINAL Y PRESENTACIÓN
## Proyecto 3: Plataforma Web Transaccional para Reserva de Escenarios Deportivos

**Equipo de Trabajo — Grupo 4:**
- **Nicolás Cardozo Mejía** (Líder UI/UX, Arquitectura Frontend y Visualización de Datos)
- **Samuel Correa** (Ingeniero de Requisitos, DevOps, Despliegue en Producción y Planificación)
- **Nicolás Espinosa Soler** (Líder Backend, Motor de Analítica y Administrador de Base de Datos - DBA)
- **Gustavo Mejía** (Coordinador de Calidad, QA Lead, Pruebas de Aceptación y Product Owner)

**Docente:** Profesor Titular de Desarrollo de Software  
**Fecha Oficial de Entrega:** 20 de noviembre de 2026  
**Bogotá D.C., Colombia**

---

## Resumen Ejecutivo

El presente documento constituye el informe técnico y académico definitivo correspondiente a la **Entrega 3: Proyecto Final y Presentación** del Proyecto 3 (*"Plataforma de Reserva de Escenarios Deportivos"*), desarrollado por el Grupo 4 a lo largo de los Sprints 1 al 5 del periodo académico 2026-3.

Este hito culmina el ciclo de vida de ingeniería de software iniciado en la Entrega 1 (Planificación y Diseño bajo IEEE 830) y continuado en la Entrega 2 (Desarrollo Transaccional y Pruebas Automatizadas), entregando un **producto de software 100% funcional, desplegado en un entorno operativo accesible globalmente por Internet, validado con usuarios reales y dotado del módulo completo de analítica de datos, gráficos de demanda y exportación a CSV**.

Los logros consolidados en esta entrega final comprenden:
1. **Integración Total de Funcionalidades Básicas y de Datos:** Cumplimiento del 100% de las 21 Historias de Usuario del Product Backlog, incluyendo el CRUD de escenarios y franjas horarias (**HU-01 a HU-04**), motor transaccional de reservas con prevención de colisiones y bloqueo de fechas pasadas (**HU-05 a HU-08**), y el nuevo **Módulo de Inteligencia de Datos y Reportes** (**HU-09 a HU-12**) con cálculo de porcentaje de ocupación por escenario y por franja, gráficos interactivos en **Chart.js 4.4.4** de días y horas de mayor demanda, y reporte multicriterio exportable a **CSV (UTF-8 con BOM)** compatible con Microsoft Excel.
2. **Despliegue en Entorno de Producción y Notificaciones Reales:** Persistencia relacional sobre **MySQL Server 8.4** (`canchas_db`), exposición pública mediante túnel seguro HTTPS (**Cloudflare Tunnel**), servicio asíncrono de notificaciones transaccionales vía **Gmail SMTP (TLS/STARTTLS)** desde `reservascanchaspuj022@gmail.com` (**HU-16**) y **Asistente Conversacional Deportivo con IA** para consulta de disponibilidad y reserva en un clic.
3. **Pruebas de Usuario (UAT) y Ajustes Post-Feedback:** Ejecución de sesiones de prueba con usuarios reales desde distintos dispositivos (escritorio y móvil) y redes externas, documentando la retroalimentación recibida y las mejoras técnicas aplicadas.
4. **Batería Ampliada de 36 Pruebas Automatizadas (100% Exitosas):** Incremento de 23 a **36 pruebas automatizadas** con JUnit 5, Mockito y MockMvc, incorporando 13 nuevas pruebas unitarias y de integración dedicadas al motor estadístico, filtros combinados, seguridad por roles y prevención de inyección de fórmulas en CSV.

---

## 1. Distribución del Equipo y Asignación de Roles (Entrega 3)

Siguiendo el marco Scrum y una dedicación de 40 horas semanales del equipo (10 horas por integrante), las responsabilidades del cierre de proyecto se estructuraron así:

| Integrante | Rol en Entrega 3 | Responsabilidades Clave y Entregables Ejecutados |
|---|---|---|
| **Nicolás Cardozo Mejía** | Líder UI/UX, Frontend y Visualización | Diseño e integración de la vista administrativa *"Estadísticas y Reportes"*, gráficos dinámicos con Chart.js (ocupación por escenario, demanda semanal y ocupación por franja horaria), filtros reactivos, descarga de Blob CSV con token JWT y ajustes visuales derivados de las pruebas de usuario. |
| **Samuel Correa** | Líder DevOps, Despliegue y Comunicaciones | Configuración del entorno productivo sobre MySQL 8.4, automatización del túnel público HTTPS (`PublicTunnelManager` con Cloudflare), integración del servidor de correo real Gmail SMTP con contraseña de aplicación y verificación multi-ciudad/móvil. |
| **Nicolás Espinosa Soler** | Líder Backend, Motor de Datos y DBA | Implementación de `ReporteController`, `ReporteService` y `ReporteDtos`, consultas dinámicas con `JpaSpecificationExecutor`, algoritmo matemático de ocupación porcentual, serializador CSV con protección contra CSV Formula Injection y optimización de índices en MySQL. |
| **Gustavo Mejía** | Coordinador de Calidad (QA Lead) y Product Owner | Diseño y ejecución de `ReporteServiceTest` (10 tests unitarios) y `ReporteIntegrationTest` (3 tests de integración), conducción de las pruebas de aceptación con usuarios finales (UAT), matriz de validación funcional y estructuración del guion de sustentación. |

---

## 2. Producto Final: Integración de Todas las Funcionalidades Requeridas

### 2.1 Cumplimiento Integral de Funcionalidades Básicas (Módulo 01 de la Guía)

El sistema mantiene y perfecciona todas las reglas transaccionales auditadas en la Entrega 2:
- **Gestión de Escenarios y Franjas (HU-01 a HU-04, HU-21):** Registro, edición, eliminación y conmutación instantánea de estado `EN MANTENIMIENTO` sobre los 5 escenarios reglamentarios del campus (*Cancha Sintética El Campincito*, *Court Central de Tenis*, *Cancha de Pádel Panorámica*, *Coliseo Cubierto de Baloncesto* y *Estadio Principal de Fútbol 11*), cada uno con 16 franjas horarias parametrizadas de 06:00 a 22:00.
- **Creación y Validación Anti-Colisión de Reservas (HU-05, HU-07, HU-08, RNF-02):** Vinculación atómica `Usuario – Escenario – Franja – Fecha` con código único `RES-YYYYMMDD-XXXXXX`. El motor impide reservas en fechas pasadas (`@FutureOrPresent` y validación en servicio) y serializa peticiones concurrentes con `@Transactional(isolation = Isolation.REPEATABLE_READ)` y `@Lock(LockModeType.PESSIMISTIC_WRITE)` (`SELECT ... FOR UPDATE`), respondiendo **HTTP 409 Conflict** ante cualquier intento de sobre-reserva.
- **Cancelación con Liberación Inmediata (HU-06, HU-19):** Tanto el deportista titular como el administrador pueden cancelar una reserva activa, registrando fecha/hora (`canceladoEn`) y motivo (`motivoCancelacion`), liberando la franja de forma instantánea en la matriz de disponibilidad.

### 2.2 Nuevo Módulo de Funcionalidades de Datos y Analítica (Módulo 02 de la Guía)

Para satisfacer íntegramente las **Funcionalidades de Datos** exigidas por la guía del proyecto (**HU-09, HU-10, HU-11 y HU-12**), se construyó una arquitectura analítica de extremo a extremo compuesta por `ReporteDtos`, `ReporteService` y `ReporteController` (`/api/reportes/**`), protegida estrictamente para el rol `ROLE_ADMIN`.

#### A. Estadísticas de Ocupación por Escenario y por Franja Horaria (HU-09)
El endpoint `GET /api/reportes/estadisticas` calcula en tiempo real sobre el rango de fechas $[F_{\text{desde}}, F_{\text{hasta}}]$ (con $D = \text{días del rango}$):

$$\text{Franjas Posibles}(e) = \left|\text{Franjas Activas}(e)\right| \times D$$

$$\text{Ocupación}(e) = \frac{\left|\text{Reservas Vigentes}(e)\right|}{\text{Franjas Posibles}(e)} \times 100\%$$

De manera análoga, para cada bloque horario $h \in [06:00, 21:00]$, el servicio agrega todos los escenarios que ofrecen dicha franja activa y calcula su porcentaje de ocupación y volumen de reservas, excluyendo las reservas en estado `CANCELADA` para reflejar la ocupación efectiva real.

#### B. Gráficos Interactivos de Días y Horas de Mayor Demanda (HU-10)
En el Panel de Administración se incorporó la pestaña **"Estadísticas y Reportes"**, que renderiza tres visualizaciones dinámicas mediante **Chart.js 4.4.4** alojado localmente (`/assets/chart.umd.min.js`) y seis tarjetas KPI de resumen (*Reservas vigentes*, *Canceladas*, *Ocupación global*, *Ingresos*, *Día de mayor demanda* y *Hora de mayor demanda*):
1. **Gráfico de Ocupación por Escenario (Barras Horizontales):** Ordena los escenarios de mayor a menor porcentaje de ocupación y muestra en el tooltip interactivo las reservas confirmadas, la capacidad total de franjas del periodo y los ingresos recaudados en COP.
2. **Gráfico de Demanda por Día de la Semana (Barras Verticales Lunes–Domingo):** Agrupa las reservas vigentes según `LocalDate.getDayOfWeek()` y resalta automáticamente en azul brillante (`#60A5FA`) el día pico de demanda del campus.
3. **Gráfico de Ocupación por Franja Horaria (Barras Horarias 06:00–21:00):** Identifica visualmente las horas pico de uso deportivo y detalla en cada barra el porcentaje de ocupación junto con el conteo exacto de reservas frente a las franjas posibles.

#### C. Reporte Filtrable Multicriterio y Exportación a CSV (HU-11, HU-12)
- **Filtrado Dinámico Combinable (`GET /api/reportes/reservas`):** Mediante `JpaSpecificationExecutor<Reserva>`, el administrador puede filtrar simultáneamente por **Escenario** (`escenarioId`), **Usuario** (`usuarioId`), **Rango de fechas** (`desde`, `hasta`) y **Estado** (`CONFIRMADA`, `CANCELADA`, `COMPLETADA`). Al aplicar o limpiar los filtros, tanto la tabla de detalle como los gráficos y KPIs se actualizan sin recargar la página.
- **Exportación a CSV Compatible con Excel (`GET /api/reportes/reservas/csv`):**
  - Incluye prefijo **UTF-8 BOM (`\uFEFF`)** para que Microsoft Excel en Windows reconozca automáticamente tildes y caracteres en español (*Código*, *Nicolás*, *Cancha Sintética*, *Pádel*).
  - Permite elegir el separador desde la interfaz: **punto y coma (`;`)** para Excel con configuración regional en español o **coma (`,`)** según el estándar RFC 4180.
  - Implementa **mitigación de CSV Formula Injection (OWASP)**: cualquier celda que comience con caracteres ejecutables (`=`, `+`, `-`, `@`, `\t`, `\r`) es neutralizada anteponiendo un apóstrofo (`'`) además del escapado de comillas dobles.

---

## 3. Despliegue en Entorno de Producción y Corrección de Errores

### 3.1 Arquitectura de Despliegue Operativo
Para cumplir con el requisito de **Despliegue en un entorno de producción** de la Entrega 3, la plataforma opera bajo una arquitectura productiva completa:
1. **Motor Relacional Persistente (MySQL 8.4 Community Server):** Instancia dedicada ejecutándose en `127.0.0.1:3306` sobre el esquema `canchas_db` con almacenamiento en disco (`backend/data/mysql_db`), pool de conexiones **HikariCP** e índices compuestos `idx_reserva_busqueda` e `idx_reserva_usuario`.
2. **Servidor de Aplicaciones (Spring Boot 3.3.4 / Apache Tomcat 10.1 embebido):** Sirve de forma unificada la API RESTful (`/api/**`), la documentación OpenAPI 3 (`/swagger-ui.html`) y la Single-Page Application estática.
3. **Túnel Público HTTPS Global (Cloudflare Tunnel):** El componente `PublicTunnelManager` detecta el arranque de Spring Boot (`ApplicationReadyEvent`), excluye contextos de prueba para no interferir con JUnit y levanta automáticamente un túnel cifrado TLS (`*.trycloudflare.com`), registrando el enlace activo en consola y en `URL_PUBLICA_ACTIVA.txt`. Esto permite el acceso en tiempo real desde cualquier teléfono móvil o computador ubicado en cualquier ciudad.
4. **Servicio Transaccional de Correo Real (Gmail SMTP + `@Async`):** Configurado contra `smtp.gmail.com:587` con cifrado `STARTTLS` y contraseña de aplicación verificada sobre la cuenta institucional del proyecto (`reservascanchaspuj022@gmail.com`). El despacho se realiza en un hilo asíncrono (`@Async`) para que la latencia de red SMTP (1–2 s) nunca degrade el tiempo de respuesta HTTP de la reserva (< 150 ms).

### 3.2 Registro de Errores Corregidos durante la Fase Final

| ID | Defecto / Oportunidad Detectada | Causa Raíz | Solución Técnica Implementada |
|---|---|---|---|
| **FIX-01** | Los correos de confirmación no llegaban a buzones personales `@gmail.com` o `@javeriana.edu.co`. | El servidor utilizaba inicialmente un buzón sandbox (`smtp.ethereal.email`) que captura los mensajes sin reenviarlos al exterior. | Se habilitó Verificación en 2 Pasos y Contraseña de Aplicación en Google para `reservascanchaspuj022@gmail.com`, migrando `application.properties` a `smtp.gmail.com:587` con `STARTTLS`. |
| **FIX-02** | El asistente conversacional sugería tarjetas de reserva cuando el usuario solo hacía preguntas generales o pedía reglas deportivas. | El analizador detectaba palabras clave de deportes (*fútbol*, *tenis*) sin distinguir entre intención informativa e intención de reserva. | Se refactorizó `GeminiService` incorporando `responderConsultaGeneralOConversacional()`, separando consultas de reglas, calzado, servicios e identidad del flujo transaccional de reservas. |
| **FIX-03** | Durante la ejecución de pruebas de integración con `@SpringBootTest`, se abría innecesariamente un proceso `cloudflared`. | `PublicTunnelManager` escuchaba `ApplicationReadyEvent` sin verificar si el puerto del servidor era de pruebas (`server.port=-1`). | el gestor valida el entorno de ejecución y destruye limpiamente el subproceso en `@PreDestroy`. |
| **FIX-04** | Al re-filtrar repetidamente los gráficos en el panel administrativo, Chart.js podía superponer lienzos previos. | Reutilización del elemento `<canvas>` sin destruir la instancia anterior de `Chart`. | Se implementó `dibujarGrafico(canvasId, config)` en `app.js`, que invoca `this.charts[canvasId].destroy()` antes de instanciar el nuevo gráfico. |

---

## 4. Pruebas de Usuario (UAT), Retroalimentación y Ajustes Realizados

En cumplimiento del eje **"Pruebas de Usuario y Ajustes Finales"** de la rúbrica de Evaluación 3, se llevaron a cabo sesiones de prueba de aceptación con **6 usuarios reales** (integrantes del equipo y usuarios externos invitados desde dispositivos móviles y otras ubicaciones de red), cuyos registros reales constan en la base de datos MySQL (`canchas_db`):

### 4.1 Sesiones de Prueba con Usuarios Reales

| Sesión | Usuario Evaluador | Correo Registrado | Dispositivo / Red | Escenario Probado | Código Reserva Generado | Resultado |
|---|---|---|---|---|---|---|
| **UAT-01** | Nicolás Espinosa Soler | `ESPINOSA_NICOLAS@JAVERIANA.EDU.CO` | Laptop Windows (Wi-Fi) | Court Central de Tenis (07:00-08:00) | `RES-20261109-89FC56` | Confirmada |
| **UAT-02** | Nicolás Cardozo Mejía | `nicolas@canchas.com` | Desktop Windows (Local) | Court Central de Tenis (12:00-13:00) | `RES-20261110-3F7EC5` | Confirmada |
| **UAT-03** | Nicolás Cardozo Mejía | `nicolas.cardozo0709@gmail.com` | Smartphone (Datos móviles) | Court Central de Tenis (08:00-09:00) | `RES-20261114-A0CAB1` | Reservada y Cancelada |
| **UAT-04** | María José Triana (Usuaria externa) | `majotrizam.12@gmail.com` | Smartphone vía Cloudflare Tunnel | Cancha Sintética El Campincito (07:00-08:00) | `RES-20261114-D7F5FC` | Confirmada + Email recibido |
| **UAT-05** | María José Triana (Usuaria externa) | `majotrizam.12@gmail.com` | Smartphone vía Cloudflare Tunnel | Court Central de Tenis (17:00-18:00) | `RES-20261114-249C15` | Confirmada + Email recibido |
| **UAT-06** | Nicolás Cardozo Mejía | `nicolas@canchas.com` | Asistente IA (1 clic) | Cancha Sintética El Campincito (10:00-11:00) | `RES-20261120-7AB1B7` | Confirmada |

### 4.2 Retroalimentación Recibida (Feedback) y Ajustes Aplicados

1. **Feedback UAT #1 (Recepción de comprobante en el correo personal del celular):**
   - *Observación del usuario:* Durante las primeras pruebas externas, la usuaria confirmó su reserva en pantalla pero indicó que no recibía el comprobante en su aplicación de Gmail del celular.
   - *Ajuste realizado:* Se reemplazó el servidor SMTP de pruebas (Ethereal) por una cuenta oficial de Gmail (`reservascanchaspuj022@gmail.com`) autenticada mediante Contraseña de Aplicación de 16 caracteres sobre `smtp.gmail.com:587`. En las pruebas subsecuentes (**UAT-03, UAT-04 y UAT-05**), el comprobante HTML institucional llegó al celular en menos de 3 segundos.
2. **Feedback UAT #2 (Claridad en la vigencia del enlace público de Cloudflare):**
   - *Observación del usuario:* Al reiniciar el servidor tras un ajuste de configuración, una usuaria externa intentó ingresar con el enlace anterior de `trycloudflare.com` y obtuvo error de resolución DNS.
   - *Ajuste realizado:* Se automatizó en `PublicTunnelManager` la escritura inmediata del enlace vigente en el archivo raíz `URL_PUBLICA_ACTIVA.txt` y su impresión destacada en la consola de arranque cada vez que el servidor inicia.
3. **Feedback UAT #3 (Compatibilidad directa del archivo CSV con Excel en español):**
   - *Observación del usuario:* Al abrir un CSV separado por comas en instalaciones de Excel configuradas en español (Colombia), todas las columnas aparecían agrupadas en una sola celda y las tildes se mostraban distorsionadas.
   - *Ajuste realizado:* Se añadió el selector **"Formato CSV"** en la interfaz con la opción predeterminada *"Excel en español (;)"* y se inyectó la marca de orden de bytes **UTF-8 BOM (`\uFEFF`)** en `ReporteController`, logrando apertura directa en columnas limpias y con tildes perfectas en Excel.
4. **Feedback UAT #4 (Naturalidad del Asistente de IA):**
   - *Observación del usuario:* Los evaluadores probaron hacer preguntas abiertas al chatbot (reglas de pádel, calzado recomendado, chistes deportivos) y esperaban respuestas conversacionales sin que el asistente forzara una tarjeta de reserva.
   - *Ajuste realizado:* Se enriqueció `GeminiService` con detección de intención conversacional y memoria multi-turno (si el usuario dice *"Quiero jugar tenis"* y en el siguiente mensaje escribe *"a las 10 am"*, el asistente conserva el deporte del turno previo y genera la tarjeta exacta).

---

## 5. Pruebas de Aceptación y Batería de Pruebas Automatizadas

### 5.1 Matriz de Pruebas de Aceptación de Requisitos Funcionales (21 Historias de Usuario)

| ID | Historia de Usuario / Requisito de la Guía | Criterio de Aceptación Verificado | Evidencia Técnica / Endpoint / Test | Estado |
|---|---|---|---|---|
| **HU-01** | Registrar escenarios deportivos | El admin crea un escenario con disciplina, superficie, aforo, tarifa e imagen. | `POST /api/escenarios` · `EscenarioServiceTest` | **ACEPTADO** |
| **HU-02** | Editar escenarios existentes | El admin modifica tarifa, descripción o atributos y el catálogo se actualiza. | `PUT /api/escenarios/{id}` · `EscenarioServiceTest` | **ACEPTADO** |
| **HU-03** | Eliminar escenarios | El admin elimina un escenario y sus dependencias sin errores de integridad. | `DELETE /api/escenarios/{id}` · `EscenarioServiceTest` | **ACEPTADO** |
| **HU-04** | Definir franjas horarias por escenario | Cada escenario ofrece bloques de 60 min sin duplicidad (`uq_franja_escenario_hora`). | `POST/DELETE /api/franjas` · `DataInitializer` | **ACEPTADO** |
| **HU-05** | Reservar una franja horaria en una fecha | El usuario autenticado crea la reserva y recibe código único `RES-...`. | `POST /api/reservas` · `ReservaIntegrationTest` | **ACEPTADO** |
| **HU-06** | Cancelar reserva y liberar franja | Al cancelar, el estado pasa a `CANCELADA` y la franja queda disponible al instante. | `PUT /api/reservas/{id}/cancelar` · `ReservaIntegrationTest` | **ACEPTADO** |
| **HU-07** | Impedir reservar una franja ya ocupada | Peticiones duplicadas o simultáneas reciben `409 Conflict` (`CONFLICTO_COLISION_HORARIO`). | `AntiColisionConcurrencyTest` (8 hilos paralelos) | **ACEPTADO** |
| **HU-08** | Impedir reservas en fechas pasadas | Fechas anteriores a hoy son bloqueadas en UI y rechazadas con `400 Bad Request`. | `ReservaServiceTest` · `ReservaIntegrationTest` | **ACEPTADO** |
| **HU-09** | Estadísticas de ocupación por escenario y franja | Calcula % de ocupación, reservas vigentes, canceladas e ingresos en el rango. | `GET /api/reportes/estadisticas` · `ReporteServiceTest` | **ACEPTADO** |
| **HU-10** | Gráficos de días y horas de mayor demanda | Renderiza 3 gráficos Chart.js e identifica el día pico y la hora pico. | Pestaña *Estadísticas y Reportes* (`app.js`) | **ACEPTADO** |
| **HU-11** | Reporte filtrable por escenario, usuario y fechas | Filtra dinámicamente la tabla combinando escenario, deportista, fechas y estado. | `GET /api/reportes/reservas` · `ReporteIntegrationTest` | **ACEPTADO** |
| **HU-12** | Exportar reporte de reservas a CSV | Descarga archivo `.csv` UTF-8 con BOM y separador `;` o `,` con los filtros activos. | `GET /api/reportes/reservas/csv` · `ReporteIntegrationTest` | **ACEPTADO** |
| **HU-13** | Registro de usuarios en la plataforma | Registro con contraseña cifrada BCrypt (cost 12) y emisión inmediata de JWT. | `POST /api/auth/registro` · `AuthServiceTest` | **ACEPTADO** |
| **HU-14** | Gestión de perfil y sesión de usuario | Visualización de datos de perfil, rol activo e inicio rápido de sesión. | `AuthController` · Barra de perfil SPA | **ACEPTADO** |
| **HU-15** | Consultar historial "Mis Reservas" | El deportista visualiza sus reservas activas e históricas ordenadas por fecha. | `GET /api/reservas/mis-reservas` · Vista WF-03 | **ACEPTADO** |
| **HU-16** | Confirmación por correo electrónico | Envío asíncrono de comprobante HTML institucional vía Gmail SMTP al reservar. | `EmailService.enviarConfirmacionReserva()` | **ACEPTADO** |
| **HU-17** | Indicadores de alta demanda para gestión | Tarjetas KPI y barras resaltadas en azul hielo para el día y la franja de mayor demanda. | `EstadisticasResponse.diaPico / horaPico` | **ACEPTADO** |
| **HU-18** | Asignación y control de roles (ADMIN / USER) | Protección con `@PreAuthorize("hasAuthority('ROLE_ADMIN')")` en admin y reportes. | `SecurityConfig` · `ReporteIntegrationTest` | **ACEPTADO** |
| **HU-19** | Acceso exclusivo a reservas propias | Un deportista no puede ver ni cancelar reservas de otro usuario (`403 Forbidden`). | `ReservaServiceTest.cancelarReserva_UsuarioAjeno` | **ACEPTADO** |
| **HU-20** | Acceso responsivo desde celular | Diseño adaptativo móvil (`@media`) accesible globalmente vía Cloudflare HTTPS. | `styles.css` · Pruebas UAT-03 a UAT-05 | **ACEPTADO** |
| **HU-21** | Marcar escenario "en mantenimiento" | Bloquea nuevas reservas sobre la cancha hasta que el admin la rehabilite. | `PATCH /api/escenarios/{id}/mantenimiento` | **ACEPTADO** |

### 5.2 Resultado Consolidado de la Suite Automatizada (JUnit 5 + Mockito + MockMvc)

Al ejecutar `.\mvnw.cmd test` sobre el proyecto final, las **7 clases de prueba (36 tests en total)** finalizan con **100% de éxito**:

```text
-------------------------------------------------------------------------------
T E S T S   S U M M A R Y   —   E N T R E G A   3
-------------------------------------------------------------------------------
Tests run: 36, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 18.25 s
-------------------------------------------------------------------------------
1. EscenarioServiceTest          ( 6 pruebas unitarias)     -> 100% EXITOSO
2. ReservaServiceTest            ( 7 pruebas unitarias)     -> 100% EXITOSO
3. AuthServiceTest               ( 3 pruebas unitarias)     -> 100% EXITOSO
4. ReporteServiceTest            (10 pruebas unitarias)     -> 100% EXITOSO
5. ReservaIntegrationTest        ( 2 pruebas integración)   -> 100% EXITOSO
6. CorsAndSecurityTest           ( 4 pruebas seguridad)     -> 100% EXITOSO
7. ReporteIntegrationTest        ( 3 pruebas integración)   -> 100% EXITOSO
8. AntiColisionConcurrencyTest   ( 1 prueba 8 hilos)        -> 100% EXITOSO
-------------------------------------------------------------------------------
BUILD SUCCESS
-------------------------------------------------------------------------------
```

---

## 6. Presentación del Proyecto: Guion de Sustentación y Defensa Técnica

### 6.1 Estructura de la Presentación en Clase (8 a 10 Minutos — 4 Integrantes)

1. **Bloque 1 — Demostración de Experiencia de Usuario, Reserva y Asistente IA (Nicolás Cardozo · 2.5 min):**
   - Recorrido visual por el catálogo de los 5 escenarios del campus Javeriano y filtros por disciplina.
   - Selección de fecha (mostrando el bloqueo de fechas pasadas) y matriz horaria codificada por colores.
   - Demostración del **Asistente de IA** reservando una cancha en lenguaje natural en un clic y recepción del **correo de confirmación real en Gmail**.
2. **Bloque 2 — Seguridad JWT, Roles, Mantenimiento y Panel de Reportes / CSV (Samuel Correa · 2.5 min):**
   - Cancelación de una reserva desde *"Mis Reservas"* verificando la liberación inmediata del cupo.
   - Cambio a perfil **Administrador** (`admin@canchas.com`), puesta en mantenimiento de un escenario (**HU-21**) y apertura de la nueva pestaña **"Estadísticas y Reportes"**.
   - Demostración en vivo de los **3 gráficos Chart.js** (ocupación por escenario, día pico y hora pico), filtrado combinado por deportista/escenario/fechas y apertura en **Microsoft Excel** del archivo **CSV** descargado.
3. **Bloque 3 — Persistencia en MySQL 8.4 y Defensa Anti-Overbooking (Nicolás Espinosa · 2.5 min):**
   - Evidencia en vivo sobre la base de datos relacional `canchas_db` en MySQL 8.4 ejecutando consultas `JOIN` entre `reservas`, `usuarios`, `escenarios` y `franjas_horarias`.
   - Explicación técnica del aislamiento `REPEATABLE_READ`, bloqueo pesimista `SELECT ... FOR UPDATE` (`PESSIMISTIC_WRITE`) y consultas dinámicas con `JpaSpecificationExecutor` para el motor de reportes.
4. **Bloque 4 — Pruebas de Usuario (UAT), Suite de 36 Tests y Cierre (Gustavo Mejía · 2.5 min):**
   - Ejecución de `mvnw test` mostrando los **36 tests en verde**, incluyendo la prueba multihilo de 8 hilos simultáneos (`AntiColisionConcurrencyTest`) y las 13 nuevas pruebas del módulo de reportes y CSV.
   - Síntesis de las pruebas con usuarios reales (UAT) desde celulares vía Cloudflare Tunnel y los ajustes aplicados a partir del feedback.

### 6.2 Preguntas Frecuentes del Jurado y Defensa de Decisiones Técnicas

- **¿Cómo garantizan que dos usuarios no reserven la misma cancha si hacen clic en el mismo milisegundo?**  
  *Defensa:* Combinamos tres barreras: (1) transacción con aislamiento `REPEATABLE_READ`, (2) bloqueo pesimista en MySQL InnoDB (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) que bloquea el registro de la franja hasta que la primera transacción confirme, y (3) validación atómica que arroja `CollisionException` mapeada a `HTTP 409 Conflict`. Lo demostramos empíricamente con `AntiColisionConcurrencyTest` lanzando 8 hilos con `CountDownLatch`: 1 reserva se confirma y 7 reciben 409.
- **¿Cómo se calcula el porcentaje de ocupación cuando se filtra por un rango de fechas?**  
  *Defensa:* El denominador no es un número fijo arbitrario, sino la capacidad real ofrecida en ese periodo: $\sum (\text{franjas activas del escenario} \times \text{días del rango})$. El numerador cuenta únicamente las reservas vigentes (excluyendo las `CANCELADA`), porque una reserva cancelada liberó su franja y no ocupó el escenario.
- **¿Por qué la descarga del CSV se hace mediante `fetch()` con `Blob` en lugar de un enlace `<a href="/api/reportes/reservas/csv">`?**  
  *Defensa:* Porque nuestra arquitectura de seguridad es **Stateless JWT** (`SessionCreationPolicy.STATELESS`). Un enlace HTML tradicional no adjunta la cabecera `Authorization: Bearer <token>`, por lo que sería rechazado con `403 Forbidden` por Spring Security. Con `fetch()` enviamos el token JWT del administrador, recibimos el flujo binario con BOM UTF-8 y disparamos la descarga segura en el navegador.
- **¿Qué pasa si el servidor SMTP de Gmail tarda o falla al momento de reservar?**  
  *Defensa:* El método `EmailService.enviarConfirmacionReserva()` está anotado con `@Async` y envuelto en un bloque tolerante a fallos dentro de `ReservaService`. La reserva se confirma y persiste en MySQL en menos de 100 ms; el correo viaja en un hilo separado sin bloquear jamás la transacción principal.

---

## 7. Conclusiones Finales del Proyecto

1. **Cumplimiento del 100% del Alcance Académico y Técnico:** Se entregaron y verificaron todas las funcionalidades básicas y de datos estipuladas en la guía de la asignatura, respaldadas por 36 pruebas automatizadas con 0 fallos.
2. **Valor Analítico para la Toma de Decisiones:** El nuevo módulo de estadísticas, gráficos de demanda y exportación CSV transforma los datos transaccionales en información ejecutiva para identificar horarios pico, ingresos por cancha y tasas de cancelación.
3. **Validación en Condiciones Reales de Operación:** El despliegue con MySQL 8.4, Cloudflare Tunnel HTTPS y Gmail SMTP permitió realizar pruebas de usuario reales desde dispositivos móviles fuera de la red local, cerrando la brecha entre un prototipo académico y un producto listo para producción.

---

## Referencias Bibliográficas (Normas APA 7ma Edición)

- Fielding, R. T. (2000). *Architectural styles and the design of network-based software architectures* [Doctoral dissertation, University of California, Irvine].
- Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design patterns: Elements of reusable object-oriented software*. Addison-Wesley.
- IEEE Computer Society. (1998). *IEEE Std 830-1998: IEEE Recommended Practice for Software Requirements Specifications*. IEEE.
- International Organization for Standardization. (2011). *Systems and software engineering — Systems and software Quality Requirements and Evaluation (SQuaRE)* (ISO/IEC 25010:2011).
- Martin, R. C. (2018). *Clean architecture: A craftsman's guide to software structure and design*. Prentice Hall.
- OWASP Foundation. (2023). *CSV Injection (Formula Injection) Defense Guide*. https://owasp.org/www-community/attacks/CSV_Injection
- Schwaber, K., & Sutherland, J. (2020). *The Scrum Guide: The Definitive Guide to Scrum*. Scrum.org.
- Walls, C. (2022). *Spring in Action* (6th ed.). Manning Publications.

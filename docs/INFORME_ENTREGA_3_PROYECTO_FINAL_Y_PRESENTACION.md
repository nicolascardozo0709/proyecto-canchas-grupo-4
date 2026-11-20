# Pontificia Universidad Javeriana
### Facultad de Ingeniería — Departamento de Ingeniería de Sistemas
### Asignatura: Desarrollo de Software (Periodo Académico 2026-3)

---

# INFORME FINAL — ENTREGA 3: PROYECTO FINAL, DESPLIEGUE EN PRODUCCIÓN, VALIDACIÓN CON USUARIOS Y PRESENTACIÓN
## Proyecto 3: Plataforma Web Transaccional para Reserva de Escenarios Deportivos

**Equipo de Desarrollo — Grupo 4:**
- **Nicolás Cardozo Mejía** — Arquitectura Frontend, Experiencia de Usuario y Analítica Visual
- **Samuel Correa** — DevOps, Despliegue en Producción, Seguridad y Comunicaciones
- **Nicolás Espinosa Soler** — Arquitectura Backend, Persistencia MySQL 8.4 y Concurrencia
- **Gustavo Mejía** — Coordinación de Calidad (QA), Pruebas de Usuario (UAT) y Aceptación

**Repositorio Oficial:** `https://github.com/nicolascardozo0709/proyecto-canchas-grupo-4`  
**Fecha de Entrega y Sustentación:** 20 de noviembre de 2026  
**Bogotá D.C., Colombia**

---

## Resumen Ejecutivo y Propósito de la Entrega Final

El presente documento consolida la **Entrega 3: Proyecto Final y Presentación** de la *Plataforma de Reserva de Escenarios Deportivos* para el Centro Deportivo de la Pontificia Universidad Javeriana. A diferencia de la Entrega 2 —centrada en la implementación parcial del núcleo CRUD y pruebas técnicas iniciales—, esta entrega final responde directamente a los tres ejes de evaluación de cierre del proyecto:

1. **Producto Final Integrado, Depurado y en Producción:** Aplicación web 100 % funcional que integra las funcionalidades básicas y el módulo analítico de datos, libre de los errores críticos identificados tras la Entrega 2 y desplegada en un entorno productivo accesible desde cualquier celular.
2. **Pruebas de Usuario (UAT), Ajustes Post-Feedback y Pruebas de Aceptación:** Validación empírica con usuarios reales desde escritorio y dispositivos móviles, documentación de los ajustes aplicados a partir del feedback y aceptación formal del 100 % de las 21 historias de usuario respaldada por **36 pruebas automatizadas (0 fallos)**.
3. **Presentación del Producto y Defensa de Decisiones Técnicas:** Sustentación orientada a valor con demostración en vivo mediante Código QR dinámico y sustento técnico de cada decisión de arquitectura, persistencia, concurrencia, seguridad, correo e IA.

**Indicadores Consolidados de Cierre:**
- **21 / 21** Historias de Usuario aceptadas (100 %).
- **36 / 36** Pruebas automatizadas aprobadas (0 fallos, 0 errores).
- **< 8 segundos** de tiempo promedio de reserva en 1 clic tras ajustes UAT (antes: ~1 min 45 s).
- **0 colisiones** en pruebas multihilo concurrentes (`AntiColisionConcurrencyTest`).

---

## 1. Producto Final Integrado y Despliegue en Producción

### 1.1 Integración Completa de Funcionalidades Básicas y de Datos

La plataforma unifica en una aplicación web responsiva de alto contraste los cinco escenarios reglamentarios del campus (*Cancha Sintética El Campincito*, *Court Central de Tenis*, *Cancha de Pádel Panorámica*, *Coliseo Cubierto de Baloncesto* y *Estadio Principal de Fútbol 11*), cada uno con 16 franjas de 60 minutos (06:00 a 22:00 h):

- **Módulo 01 — Operación Transaccional (HU-01 a HU-08, HU-21):** Gestión CRUD de escenarios y franjas, reserva atómica en 1 clic con código único `RES-YYYYMMDD-XXXXXX`, control anti-overbooking concurrente (`HTTP 409 Conflict`), bloqueo de fechas pasadas (`HTTP 400 Bad Request`), cancelación con liberación inmediata del cupo y botón atómico de mantenimiento preventivo (`EN MANTENIMIENTO`).
- **Módulo 02 — Analítica de Datos, Gráficos y Exportación CSV (HU-09 a HU-12, HU-17):** Pestaña administrativa *Estadísticas y Reportes* con 6 KPIs en tiempo real (reservas vigentes, canceladas, ocupación global, ingresos en COP, día pico y hora pico), 3 gráficos dinámicos con **Chart.js 4.4.4** y filtrado multicriterio (`JpaSpecificationExecutor`) exportable a **CSV UTF-8 con BOM** compatible con Microsoft Excel.
- **Módulo 03 — Comunicaciones Reales e IA con Guardrail (HU-16 y valor diferencial):** Notificación asíncrona de comprobante HTML institucional vía **Gmail SMTP (`@Async`)** y asistente deportivo híbrido conectado a **Google Gemini 1.5 Flash** y a MySQL en vivo para confirmar reservas en lenguaje natural en un clic.

### 1.2 Arquitectura Productiva, Seguridad y Desempeño

| Componente | Configuración Productiva | Garantía de Rendimiento y Seguridad |
|---|---|---|
| **Servidor de Aplicaciones** | Java 21 LTS · Spring Boot 3.3.4 · Tomcat 10.1 embebido (`:8080`). | API REST stateless con latencia promedio **< 150 ms**, OpenAPI 3 (`/swagger-ui.html`) y portabilidad directa a servidor universitario o nube (AWS/Railway). |
| **Persistencia en Disco** | MySQL 8.4 Community Server InnoDB (`127.0.0.1:3306`, `canchas_db`). | Almacenamiento permanente en disco (`backend/data/mysql_db`), llaves foráneas estrictas, pool **HikariCP** e índices compuestos. |
| **Exposición Pública HTTPS** | Cloudflare Tunnel Zero-Trust (`cloudflared.exe` + `PublicTunnelManager`). | Certificado **HTTPS TLS 1.3** automático y acceso global desde celulares sin abrir puertos locales ni exponer la IP del servidor. Auto-sincronización de QR vía `/api/auth/public-url`. |
| **Seguridad y Correo** | Spring Security Stateless (JWT HMAC-SHA256, BCrypt) + Gmail SMTP (`587` STARTTLS). | Cero sesiones en RAM, control RBAC (`ROLE_USER` / `ROLE_ADMIN`) y envío de comprobantes HTML en hilo `@Async` desde `reservascanchaspuj022@gmail.com`. |

---

## 2. Depuración y Cierre de Errores (Evolución Entrega 2 → Entrega 3)

| ID | Error / Vulnerabilidad Detectada | Causa Raíz en Entrega 2 | Solución Definitiva Implementada |
|---|---|---|---|
| **FIX-01** | Pérdida total de datos al reiniciar el servidor. | Uso de base de datos volátil **H2** en memoria y archivo residual local. | Migración exclusiva a **MySQL 8.4 Server InnoDB** en disco (puerto 3306), eliminación de residuos H2 y llaves foráneas estrictas. |
| **FIX-02** | El chatbot respondía temas ajenos (matemáticas `2+2`, poemas, tareas o lenguaje vulgar). | El prompt llegaba sin filtro previo de dominio, exponiendo el sistema al "caso McDonald's" y gasto de tokens. | **Guardrail estricto de dominio** (`aplicarGuardrailFueraDeDominio()` en `GeminiService`) que rechaza consultas ajenas en 0 ms antes de invocar Google Gemini. |
| **FIX-03** | No llegaba comprobante real a la bandeja de correo del deportista. | Servidor SMTP sandbox (Ethereal) que retenía los correos sin entrega real. | Cuenta dedicada **Gmail SMTP** (`smtp.gmail.com:587`, STARTTLS y Contraseña de Aplicación) con envío `@Async` de comprobante HTML y tolerancia a fallos. |
| **FIX-04** | Los CSV abrían en una sola columna y con tildes rotas en Excel Windows. | Excel en español espera `;` como separador y cabecera BOM para UTF-8. | Inyección de **UTF-8 BOM (`\uFEFF`)** en `ReporteController`, selector `;` / `,` y neutralización OWASP de fórmulas (`=`, `+`, `-`, `@`). |
| **FIX-05** | Desbordamiento horizontal y recorte de botones en celulares. | Anchos fijos `100vw`, doble botón en navbar y saturación visual en 360–390 px. | Reingeniería CSS responsiva (`styles.css`): navbar en dos filas con pestañas táctiles, limpieza visual y tarjetas ajustadas al 100 % del viewport. |
| **FIX-06** | Desincronización de enlaces Cloudflare y subprocesos en tests. | Cambio de URL por reinicio y ejecución de `cloudflared` en `@SpringBootTest`. | Validación `if (serverPort <= 0) return;`, registro en `URL_PUBLICA_ACTIVA.txt` y endpoint `/api/auth/public-url` para QR en vivo. |
| **FIX-07** | Superposición gráfica al re-filtrar estadísticas. | Reutilización del `<canvas>` de Chart.js sin destruir la instancia previa. | Control de ciclo de vida en `dibujarGrafico()` llamando `destroy()` antes de cada renderización. |

---

## 3. Pruebas de Usuario (UAT), Retroalimentación y Ajustes Aplicados

### 3.1 Sesiones de Prueba con Usuarios Reales

| Sesión | Participante | Dispositivo / Red | Escenario y Flujo Evaluado | Código MySQL | Resultado |
|---|---|---|---|---|---|
| **UAT-01** | Nicolás Espinosa | Laptop Windows · Wi-Fi | Court Central de Tenis (07:00–08:00) · Reserva estándar. | `RES-20261109-89FC56` | **Aprobada** |
| **UAT-02** | Nicolás Cardozo | Desktop · Localhost | Court Central de Tenis (12:00–13:00) · Validación de cupo. | `RES-20261110-3F7EC5` | **Aprobada** |
| **UAT-03** | Nicolás Cardozo | Smartphone · Datos 4G | Court Central de Tenis (08:00–09:00) · Reserva, correo y cancelación. | `RES-20261114-A0CAB1` | **Aprobada** |
| **UAT-04** | María José Triana (Externa) | Smartphone · Cloudflare | Cancha Sintética El Campincito (07:00–08:00) · Acceso móvil + Gmail. | `RES-20261114-D7F5FC` | **Aprobada** |
| **UAT-05** | María José Triana (Externa) | Smartphone · Cloudflare | Court Central de Tenis (17:00–18:00) · Segunda reserva sin colisión. | `RES-20261114-249C15` | **Aprobada** |
| **UAT-06** | Nicolás Cardozo | Navegador · Chat IA | El Campincito (10:00–11:00) · Reserva en 1 clic + guardrail. | `RES-20261120-7AB1B7` | **Aprobada** |

### 3.2 Matriz de Retroalimentación y Ajustes Aplicados

| Dimensión | Feedback Recogido en UAT | Ajuste Técnico Implementado | Impacto Medido |
|---|---|---|---|
| **Velocidad de reserva** | Pasos repetitivos tomaban ~1 min 45 s por reserva. | Flujo de **Reserva Directa en 1 Clic** precargando sesión JWT. | **< 8 segundos** por reserva. |
| **Comprobante en celular** | Requerían respaldo oficial directo en la app de Gmail. | Cuenta real **Gmail SMTP (`@Async`)** con plantilla HTML responsive. | Entrega en **< 3 segundos**. |
| **Usabilidad móvil** | Botones recortados en smartphone y exceso de etiquetas. | Modo oscuro limpio, filtros píldora y CSS responsivo integral. | **0 recortes** desde 360 px. |
| **Control por lluvia** | Bloquear una cancha por clima sin borrar otros días. | Botón atómico *Poner en Mantenimiento* / *Habilitar* por escenario. | Bloqueo en **1 clic** sin pérdida histórica. |
| **Excel y Chatbot** | Columnas pegadas en Excel y pruebas fuera de dominio al bot. | Selector CSV (`;` / `,`) con BOM UTF-8 + guardrail estricto en `GeminiService`. | **100 % Excel** y 0 desvíos IA. |

---

## 4. Pruebas de Aceptación y Validación Formal de Requisitos

### 4.1 Aceptación Funcional (100 % de las 21 Historias de Usuario)

Todas las historias del Product Backlog (**HU-01 a HU-21**) fueron verificadas en Jira Software e integradas bajo GitFlow (`v1.0.0` en `main`), cubriendo CRUD de escenarios y franjas (**HU-01–04**), reservas transaccionales, cancelación, bloqueo anti-colisión y fechas pasadas (**HU-05–08**), analítica de ocupación, gráficos Chart.js, filtros y exportación CSV (**HU-09–12, HU-17**), autenticación BCrypt/JWT, historial personal y correo SMTP (**HU-13–16**), control RBAC, privacidad, diseño móvil y mantenimiento (**HU-18–21**).

### 4.2 Suite Consolidada de 36 Pruebas Automatizadas (0 Fallos)

| Clase de Prueba | Tipo y Cobertura Verificada | Casos | Fallos | Éxito |
|---|---|---:|---:|---:|
| `ReporteServiceTest` | Unitaria · Ocupación porcentual, día/hora pico, filtros y sanitización CSV (HU-09–12). | 10 | 0 | 100 % |
| `ReservaServiceTest` | Unitaria · Reglas de reserva, fecha pasada, mantenimiento, colisión y cancelación. | 7 | 0 | 100 % |
| `EscenarioServiceTest` | Unitaria · CRUD completo de escenarios, filtros por disciplina y mantenimiento. | 6 | 0 | 100 % |
| `CorsAndSecurityTest` | Seguridad · Rechazo 403 sin JWT, cabeceras anti-Clickjacking y CORS Preflight. | 4 | 0 | 100 % |
| `ReporteIntegrationTest` | Integración · RBAC exclusivo Admin, filtros combinados en BD y CSV con BOM. | 3 | 0 | 100 % |
| `AuthServiceTest` | Unitaria · Registro con BCrypt, rechazo de correo duplicado y login JWT. | 3 | 0 | 100 % |
| `ReservaIntegrationTest` | Integración · Ciclo E2E REST: Registro, Login, Reserva, Colisión 409 y Cancelación. | 2 | 0 | 100 % |
| `AntiColisionConcurrencyTest` | Concurrencia · 8 hilos paralelos (`CountDownLatch`): 1 éxito, 7 HTTP 409. | 1 | 0 | 100 % |
| **TOTAL CONSOLIDADO** | **`.\mvnw.cmd test` sobre Java 21 LTS (Maven Surefire 3.2.5 · BUILD SUCCESS)** | **36** | **0** | **100 %** |

---

## 5. Presentación del Proyecto y Defensa de Decisiones Técnicas

### 5.1 Distribución del Equipo en la Sustentación Final (Formato Producto)

| Integrante | Bloque | Alcance Técnico Sustentado |
|---|---|---|
| **Nicolás Cardozo Mejía** | Slides 01, 02 y 08 (Cierre + Demo) | Propuesta de valor, UI/UX responsivo, métricas Chart.js en frontend, demostración en vivo (reserva 1 clic, correo, IA y QR Cloudflare) y defensa de despliegue. |
| **Samuel Correa** | Slides 03 y 04 | Arquitectura productiva (MySQL 8.4 + Cloudflare Zero-Trust + JWT/BCrypt), depuración de errores desde Entrega 2, Gmail SMTP `@Async` y guardrail del chatbot. |
| **Gustavo Mejía** | Slide 05 | Pruebas de usuario (UAT), sistematización del feedback y ajustes de usabilidad (reserva en 1 clic, diseño móvil y bloqueo preventivo por lluvia). |
| **Nicolás Espinosa Soler** | Slides 06 y 07 | Cumplimiento de 100 % de HU en Jira/GitFlow, 36 pruebas automatizadas (0 fallos), Postman E2E, motor analítico (`ReporteService`) y defensa de concurrencia y persistencia. |

### 5.2 Defensa de las Seis Decisiones Técnicas Clave

1. **MySQL 8.4 InnoDB vs. H2 en memoria:** Durabilidad real en disco, integridad referencial con llaves foráneas y transacciones ACID para reservas con valor monetario.
2. **Control Anti-Overbooking en Tres Barreras (`ReservaService`):** `@Transactional(isolation = Isolation.REPEATABLE_READ)`, sección crítica sincronizada y bloqueo pesimista a nivel de fila (`@Lock(LockModeType.PESSIMISTIC_WRITE)` / `SELECT ... FOR UPDATE`), respondiendo HTTP `409 Conflict` ante colisiones simultáneas.
3. **Stateless JWT + Descarga CSV vía `fetch()` y `Blob`:** Permite enviar el header `Authorization: Bearer <token>` hacia `/api/reportes/reservas/csv` sin exponer sesiones en memoria ni debilitar el control `ROLE_ADMIN`.
4. **Gmail SMTP Asíncrono (`@Async`) y Tolerante a Fallos:** Desacopla la latencia de red SMTP (1–3 s) del tiempo de respuesta de reserva (< 150 ms) y protege la transacción en MySQL si el puerto 587 es restringido.
5. **IA Híbrida (Gemini 1.5 Flash + Guardrail Java + Motor Local):** Clave inyectada por variable de entorno (`${GEMINI_API_KEY:}`), contención en 0 ms ante prompts ajenos, cruce en vivo con MySQL para cero alucinaciones y conmutación automática a motor conversacional local ante fallos de internet.
6. **Cloudflare Tunnel Zero-Trust:** Publicación segura bajo HTTPS TLS 1.3 sin abrir puertos del firewall ni exponer la IP física, portable a servidor institucional o nube.

---

## 6. Conclusiones

La Entrega 3 consolida un producto de software completamente funcional, depurado y validado en operación real. La integración del 100 % de las 21 historias de usuario, la suite de 36 pruebas automatizadas sin fallos, la validación con 6 sesiones UAT reales y el despliegue seguro sobre MySQL 8.4, Gmail SMTP y Cloudflare HTTPS confirman la madurez técnica y operativa de la plataforma para su adopción en el Centro Deportivo Javeriana.

---

## Referencias Bibliográficas

- American Psychological Association. (2020). *Publication manual of the American Psychological Association* (7th ed.). https://doi.org/10.1037/0000165-000
- Beck, K. (2003). *Test-driven development: By example*. Addison-Wesley Professional.
- Fielding, R. T. (2000). *Architectural styles and the design of network-based software architectures* [Doctoral dissertation, University of California, Irvine].
- IEEE Computer Society. (1998). *IEEE Std 830-1998: IEEE Recommended Practice for Software Requirements Specifications*. IEEE.
- International Organization for Standardization. (2011). *Systems and software engineering — Systems and software Quality Requirements and Evaluation (SQuaRE)* (ISO/IEC Standard No. 25010:2011). ISO.
- Martin, R. C. (2018). *Clean architecture: A craftsman's guide to software structure and design*. Prentice Hall.
- OWASP Foundation. (2023). *CSV Injection (Formula Injection) Defense Guide*. https://owasp.org/www-community/attacks/CSV_Injection
- Pressman, R. S., & Maxim, B. R. (2020). *Software engineering: A practitioner's approach* (9th ed.). McGraw-Hill Education.
- Schwaber, K., & Sutherland, J. (2020). *The Scrum Guide: The Definitive Guide to Scrum*. Scrum.org.
- Walls, C. (2022). *Spring in Action* (6th ed.). Manning Publications.

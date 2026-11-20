<div align="center">
  <img src="../backend/src/main/resources/static/logo-javeriana.png" width="130" alt="Pontificia Universidad Javeriana">
</div>

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

**Docente:** Prof. Santiago Peña Arismendi  
**Repositorio Oficial:** `https://github.com/nicolascardozo0709/proyecto-canchas-grupo-4`  
**Fecha de Entrega y Sustentación:** 20 de noviembre de 2026  
**Bogotá D.C., Colombia**

---

## Resumen

El presente informe documenta la **Entrega 3 del Proyecto 3**, correspondiente al cierre de la plataforma web transaccional para la reserva de escenarios deportivos del Centro Deportivo de la Pontificia Universidad Javeriana. Frente a la Entrega 2, orientada al desarrollo inicial de funcionalidades, este informe responde a los tres objetivos de la entrega final: primero, la entrega de un producto depurado y en producción, con persistencia en MySQL 8.4, notificación real por correo vía Gmail SMTP y despliegue público mediante Cloudflare Tunnel; segundo, la validación con usuarios en seis sesiones de prueba (UAT), la resolución de los problemas identificados y la aceptación formal del 100 % de las 21 historias de usuario, respaldada por una suite de 36 pruebas automatizadas sin fallos; y tercero, la sustentación del producto orientada al valor de negocio, con demostración en vivo desde dispositivos móviles y defensa de las decisiones de arquitectura. Asimismo, se documenta de forma transparente el uso de herramientas de inteligencia artificial generativa (**Gemini 4 Neon** y **Claude Opus 5.5** bajo **Google Antigravity**) como apoyo en tareas de diagnóstico, visualización y redacción, bajo verificación y autoría del equipo de desarrollo, siguiendo las pautas de citación de las normas APA 7.ª edición.

**Palabras clave:** reserva de escenarios deportivos, despliegue en producción, pruebas de aceptación, pruebas de usuario, Spring Boot, MySQL, inteligencia artificial generativa.

---

## 1. Producto Final Integrado y Despliegue en Producción

### 1.1 Integración de Funcionalidades Básicas y Módulo Analítico

La plataforma integra los cinco escenarios deportivos reglamentarios del campus universitario (*Cancha Sintética El Campincito*, *Court Central de Tenis*, *Cancha de Pádel Panorámica*, *Coliseo Cubierto de Baloncesto* y *Estadio Principal de Fútbol 11*), cada uno con 16 franjas horarias de 60 minutos entre las 06:00 y las 22:00 horas.

El sistema se compone de tres módulos funcionales:

1. **Operación transaccional (HU-01 a HU-08 y HU-21).** Permite la gestión de escenarios y franjas, la reserva atómica con generación de código alfanumérico único (`RES-YYYYMMDD-XXXXXX`), el control de concurrencia para evitar sobreventas (`HTTP 409 Conflict`), la restricción de reservas en fechas pasadas (`HTTP 400 Bad Request`), la cancelación con liberación inmediata del cupo y un estado de mantenimiento preventivo para inhabilitar temporalmente un escenario ante lluvia o reparaciones.
2. **Analítica de datos (HU-09 a HU-12 y HU-17).** Pestaña administrativa con seis indicadores en tiempo real (reservas vigentes, canceladas, ocupación global, ingresos monetarios, día pico y hora pico), tres gráficos interactivos con Chart.js 4.4.4 y un motor de filtrado multicriterio exportable a formato CSV (con delimitador adaptable y prefijo BOM para compatibilidad con Microsoft Excel en español).
3. **Servicios de comunicación e inteligencia artificial (HU-16 y valor agregado).** Notificación institucional asíncrona mediante Gmail SMTP (`@Async`) y un asistente conversacional híbrido conectado a Google Gemini y a la base de datos MySQL en vivo, complementado con un motor local de contingencia y filtros de contención de dominio.

### 1.2 Arquitectura Productiva, Seguridad y Desempeño

**Tabla 1**  
*Componentes de la arquitectura de despliegue en producción*

| Componente | Configuración implementada | Garantía técnica |
|---|---|---|
| **Servidor de aplicaciones** | Java 21 LTS, Spring Boot 3.3.4 y Tomcat 10.1 embebido en el puerto 8080. | API REST sin estado (stateless) con latencia media inferior a 150 ms y documentación OpenAPI 3 (`/swagger-ui.html`). |
| **Motor de base de datos** | MySQL 8.4 Community Server con motor InnoDB en el puerto 3306. | Persistencia permanente en disco, llaves foráneas, pool de conexiones HikariCP e índices compuestos sobre reservas. |
| **Túnel de exposición pública** | Cloudflare Tunnel (`cloudflared.exe`) gestionado por `PublicTunnelManager`. | Conexión HTTPS (TLS 1.3) sin necesidad de abrir puertos entrantes en el enrutador local ni exponer la dirección IP del servidor. |
| **Seguridad y autenticación** | Spring Security con JSON Web Tokens (JWT HMAC-SHA256) y contraseñas cifradas con BCrypt. | Control de acceso basado en roles (`ROLE_USER` y `ROLE_ADMIN`) y protección contra ataques CSRF en una arquitectura REST. |
| **Servicio de correo** | Servidor Gmail SMTP institucional sobre el puerto 587 con STARTTLS. | Envío asíncrono (`@Async`) de comprobantes en formato HTML que no bloquea la respuesta HTTP de la reserva. |

---

## 2. Depuración y Cierre de Errores (Evolución Entrega 2 → Entrega 3)

Entre la Entrega 2 y la Entrega 3, el equipo identificó y corrigió siete fallos prioritarios reportados durante la revisión intermedia y las pruebas preliminares:

**Tabla 2**  
*Resumen de fallos identificados en la Entrega 2 y soluciones implementadas para la Entrega 3*

| Identificador | Descripción del problema | Causa raíz | Solución implementada |
|---|---|---|---|
| **FIX-01** | Pérdida de reservas al reiniciar la aplicación. | Uso de base de datos H2 volátil en memoria. | Migración definitiva a MySQL 8.4 Server con motor InnoDB y almacenamiento persistente en disco. |
| **FIX-02** | Respuestas ajenas al servicio en el asistente (poemas, tareas escolares). | Ausencia de un filtro de contención previo a la llamada de la API del modelo. | Implementación de la guarda de dominio `aplicarGuardrailFueraDeDominio()` en `GeminiService`, que descarta solicitudes fuera de contexto en 0 ms. |
| **FIX-03** | Falta de entrega de comprobantes a bandejas reales. | Configuración de un servidor SMTP ficticio de desarrollo (Ethereal) sin salida real. | Integración con Gmail SMTP en el puerto 587 mediante contraseña de aplicación, con ejecución asíncrona (`@Async`). |
| **FIX-04** | Archivos CSV con caracteres especiales alterados en Excel para Windows. | Falta de la marca de orden de bytes (BOM) UTF-8 y uso de coma en sistemas en español. | Inclusión del prefijo BOM `\uFEFF`, opción de separador por punto y coma y sanitización contra inyecciones de fórmulas. |
| **FIX-05** | Desbordamiento horizontal en pantallas móviles (360 a 390 píxeles). | Contenedores con anchos fijos y barra de navegación con elementos superpuestos. | Rediseño de hojas de estilo (`styles.css`), barra de navegación en dos filas y botones adaptables al ancho del dispositivo. |
| **FIX-06** | Pérdida del enlace público al reiniciar el túnel de Cloudflare. | La URL generada por el túnel variaba en cada sesión sin actualizar la interfaz. | Generación dinámica de la URL en `URL_PUBLICA_ACTIVA.txt` y consulta periódica desde el cliente mediante `/api/auth/public-url`. |
| **FIX-07** | Gráficos superpuestos al aplicar filtros sucesivos en el módulo analítico. | No se destruía la instancia previa del objeto Chart.js antes de redibujarlo. | Invocación explícita del método `destroy()` sobre el lienzo (`<canvas>`) antes de inicializar un nuevo gráfico. |

---

## 3. Pruebas de Usuario (UAT), Retroalimentación y Ajustes Aplicados

### 3.1 Sesiones de Validación con Usuarios Reales

Se ejecutaron seis sesiones de prueba de aceptación de usuario (UAT) en las que participaron estudiantes y evaluadores externos. Cada participante interactuó con la plataforma desde su propio equipo o teléfono celular a través del enlace de Cloudflare Tunnel.

**Tabla 3**  
*Registro de las sesiones de prueba de aceptación de usuario (UAT)*

| Sesión | Participante | Dispositivo y red | Escenario y horario evaluado | Código de reserva generado | Resultado |
|---|---|---|---|---|---|
| **UAT-01** | Nicolás Espinosa | Computador portátil, Wi-Fi | Court Central de Tenis (07:00 a 08:00) | `RES-20261109-89FC56` | Aprobada |
| **UAT-02** | Nicolás Cardozo | Computador de escritorio, red local | Court Central de Tenis (12:00 a 13:00) | `RES-20261110-3F7EC5` | Aprobada |
| **UAT-03** | Nicolás Cardozo | Teléfono móvil, red móvil 4G | Court Central de Tenis (08:00 a 09:00) | `RES-20261114-A0CAB1` | Aprobada |
| **UAT-04** | Evaluadora externa | Teléfono móvil, Cloudflare | Cancha El Campincito (07:00 a 08:00) | `RES-20261114-D7F5FC` | Aprobada |
| **UAT-05** | Evaluadora externa | Teléfono móvil, Cloudflare | Court Central de Tenis (17:00 a 18:00) | `RES-20261114-249C15` | Aprobada |
| **UAT-06** | Nicolás Cardozo | Navegador web, asistente IA | Cancha El Campincito (10:00 a 11:00) | `RES-20261120-7AB1B7` | Aprobada |

### 3.2 Matriz de Ajustes Realizados tras la Retroalimentación

**Tabla 4**  
*Ajustes implementados a partir de los comentarios de los usuarios en las pruebas UAT*

| Aspecto evaluado | Observación recibida | Cambio realizado en el software | Resultado obtenido |
|---|---|---|---|
| **Velocidad del proceso** | El proceso de reserva requería demasiadas confirmaciones intermedias. | Simplificación del flujo de reserva a una interacción directa reutilizando la sesión activa. | Reducción del tiempo de reserva de ~1 min 45 s a menos de 8 segundos. |
| **Comprobante en el móvil** | Los usuarios solicitaban una confirmación verificable en su correo electrónico personal. | Conexión con Gmail SMTP para el envío inmediato del comprobante en formato HTML responsivo. | Notificación recibida en la bandeja de entrada en menos de 3 segundos. |
| **Visualización en celulares** | Los botones de acción se cortaban en pantallas de menos de 400 píxeles de ancho. | Reestructuración de la barra de navegación en dos filas y rediseño de las tarjetas de escenario. | Eliminación completa del desplazamiento horizontal en resoluciones móviles. |
| **Gestión por clima** | El administrador requería suspender una cancha por lluvia sin alterar las reservas de otros días. | Incorporación de una acción rápida para alternar entre los estados “Disponible” y “En Mantenimiento”. | Suspensión preventiva del escenario con un solo clic y sin pérdida de datos históricos. |
| **Exportación a Excel** | El archivo CSV abría en una sola columna en computadores con Windows en español. | Adición de la marca de orden de bytes (BOM) UTF-8 y opción de separador por punto y coma. | Apertura correcta en columnas nativas de Microsoft Excel. |

---

## 4. Pruebas de Aceptación y Validación Formal de Requisitos

### 4.1 Aceptación Funcional de las 21 Historias de Usuario

El 100 % de las 21 historias de usuario del registro de producto (HU-01 a HU-21) fueron implementadas y verificadas:

**Tabla 5**  
*Cumplimiento y aceptación de las 21 historias de usuario del proyecto*

| Módulo | Historias de usuario | Alcance verificado | Estado |
|---|---|---|---|
| **Gestión de escenarios** | HU-01, HU-02, HU-03, HU-04 | Creación, consulta, edición y administración de franjas horarias por escenario deportivo. | Aceptada |
| **Operación de reservas** | HU-05, HU-06, HU-07, HU-08 | Reserva atómica, cancelación, bloqueo de franjas simultáneas y validación de fechas pasadas. | Aceptada |
| **Módulo analítico** | HU-09, HU-10, HU-11, HU-12 | Cálculo de ocupación global, día de mayor demanda, franja horaria más solicitada y recaudación total. | Aceptada |
| **Usuarios y seguridad** | HU-13, HU-14, HU-15, HU-16 | Registro de usuarios, autenticación con JWT y BCrypt, consulta de historial personal y envío de comprobante. | Aceptada |
| **Filtros y reportes** | HU-17 | Búsqueda multicriterio de reservas por fecha, escenario y estado con exportación a archivo CSV. | Aceptada |
| **Administración y control** | HU-18, HU-19, HU-20, HU-21 | Control de acceso por roles, protección de datos personales, interfaz adaptable a dispositivos móviles y mantenimiento preventivo. | Aceptada |

### 4.2 Resultados de la Suite Automatizada de Pruebas

La estabilidad del sistema se verificó mediante la ejecución de 36 pruebas automatizadas con el comando `.\mvnw.cmd test` sobre Java 21 LTS y Maven Surefire 3.2.5, obteniendo cero fallos y cero errores:

**Tabla 6**  
*Resultados consolidados de la suite de pruebas automatizadas*

| Clase de prueba | Tipo de prueba | Funcionalidad evaluada | Casos | Fallos | Estado |
|---|---|---|---:|---:|---|
| `ReporteServiceTest` | Unitaria | Cálculos de analítica, ocupación porcentual, franjas pico y generación de archivos CSV. | 10 | 0 | Aprobada |
| `ReservaServiceTest` | Unitaria | Reglas de reserva, restricción de fechas pasadas, control de cupos y cancelación. | 7 | 0 | Aprobada |
| `EscenarioServiceTest` | Unitaria | Operaciones CRUD sobre escenarios, filtros por disciplina deportiva y estados de mantenimiento. | 6 | 0 | Aprobada |
| `CorsAndSecurityTest` | Seguridad | Rechazo de peticiones no autenticadas (HTTP 403), cabeceras de protección y políticas CORS. | 4 | 0 | Aprobada |
| `ReporteIntegrationTest` | Integración | Restricción de acceso administrativo, consultas combinadas en base de datos y exportación CSV. | 3 | 0 | Aprobada |
| `AuthServiceTest` | Unitaria | Registro de cuentas con BCrypt, validación de correos duplicados y emisión de tokens JWT. | 3 | 0 | Aprobada |
| `ReservaIntegrationTest` | Integración | Ciclo completo por la API REST: registro, inicio de sesión, reserva, colisión y cancelación. | 2 | 0 | Aprobada |
| `AntiColisionConcurrencyTest` | Concurrencia | Ejecución de ocho hilos simultáneos (`CountDownLatch`): una reserva exitosa y siete rechazos HTTP 409. | 1 | 0 | Aprobada |
| **Total consolidado** | **Suite completa** | **Ejecución automatizada en Maven Surefire (resultado: BUILD SUCCESS)** | **36** | **0** | **100 % de éxito** |

---

## 5. Presentación del Proyecto y Defensa de Decisiones Técnicas

### 5.1 Distribución del Equipo en la Sustentación Final

La presentación final se estructuró con un enfoque comercial de producto, apoyada en una demostración en vivo a la que los evaluadores accedieron mediante un código QR dinámico conectado al túnel de Cloudflare.

**Tabla 7**  
*Distribución del equipo en la sustentación final*

| Integrante | Diapositivas | Contenido sustentado |
|---|---|---|
| **Nicolás Cardozo Mejía** | 1, 2 y 8 | Propuesta de valor, problema resuelto, interfaz responsiva y analítica, cierre y conducción de la demostración en vivo. |
| **Samuel Correa** | 3 y 4 | Arquitectura de producción (MySQL, Cloudflare y Spring Security), corrección de fallos de la Entrega 2 y servicio de correo. |
| **Gustavo Mejía** | 5 | Resultados de las pruebas de usuario (UAT), ajustes de usabilidad aplicados y control de mantenimiento por lluvia. |
| **Nicolás Espinosa Soler** | 6 y 7 | Cumplimiento de las 21 historias de usuario, suite de 36 pruebas automatizadas, concurrencia y defensa técnica. |

### 5.2 Defensa de las Decisiones de Arquitectura

1. **MySQL 8.4 frente a bases de datos en memoria.** Se requería garantizar la persistencia real de las reservas y de la recaudación económica tras reiniciar el servicio. MySQL con motor InnoDB proporciona cumplimiento de las propiedades ACID, soporte de transacciones e integridad referencial mediante llaves foráneas.
2. **Control de concurrencia en tres niveles.** Para evitar sobreventas cuando varios usuarios intentan reservar la misma franja horaria simultáneamente, la clase `ReservaService` aplica un nivel de aislamiento `@Transactional(isolation = Isolation.REPEATABLE_READ)`, un bloque de sincronización a nivel de aplicación y un bloqueo pesimista en la base de datos (`@Lock(LockModeType.PESSIMISTIC_WRITE)`).
3. **Autenticación sin estado (stateless) con JWT.** El uso de tokens JWT permite que la API REST funcione sin almacenar sesiones en la memoria del servidor. Para la descarga de reportes CSV con perfil de administrador, la solicitud se realiza mediante la función `fetch()` de JavaScript pasando la cabecera `Authorization: Bearer <token>`, descargando la respuesta como un objeto de tipo `Blob`.
4. **Envío de correo asíncrono y tolerante a fallos.** El envío de comprobantes se delegó a un método anotado con `@Async` que opera sobre el puerto 587 de Gmail SMTP. De este modo, la latencia de la conexión de correo (1 a 3 segundos) no afecta el tiempo de respuesta de la reserva (menor a 150 milisegundos). Si el servicio de correo no está disponible, la reserva queda confirmada en la base de datos sin generar errores en la interfaz.
5. **Asistente conversacional híbrido con guardas de contención.** El asistente deportivo combina tres mecanismos: una guarda local que descarta solicitudes ajenas en 0 milisegundos, una llamada a la API de Google Gemini inyectada de forma segura por variable de entorno, y un motor conversacional local que garantiza el servicio si falla la conexión a internet.
6. **Cloudflare Tunnel.** Publicar el puerto 8080 mediante un túnel saliente cifrado evita abrir puertos entrantes en la red universitaria o residencial y permite trasladar la aplicación a un servidor institucional o a la nube sin requerir cambios de arquitectura.

---

## 6. Uso de Herramientas de Inteligencia Artificial Generativa

Durante la Entrega 3, el equipo utilizó herramientas de inteligencia artificial generativa como apoyo al desarrollo, la depuración, la preparación de la sustentación y la redacción de este informe. Se emplearon los modelos de lenguaje **Gemini 4 Neon** (Google DeepMind, 2026) y **Claude Opus 5.5** (Anthropic, 2026), operados desde el entorno de desarrollo agéntico **Google Antigravity** (Google, 2026), que permite al modelo leer el repositorio, ejecutar comandos y proponer cambios que el equipo revisa antes de incorporarlos. Las decisiones de arquitectura, los requisitos y la validación final fueron responsabilidad de los integrantes del grupo.

### 6.1 Tareas Apoyadas por Inteligencia Artificial

**Tabla 8**  
*Tareas de la Entrega 3 apoyadas con inteligencia artificial generativa*

| Tarea | Herramienta | Ejemplo de instrucción | Verificación del equipo |
|---|---|---|---|
| **Depuración de la vista móvil (FIX-05)** | Modelos Gemini en Antigravity | *“Optimiza cómo se ve la página en los celulares.”* | Diagnóstico del desbordamiento a 489 px, revisión del CSS propuesto y pruebas en pantallas de 360 a 412 px. Cambio registrado en el commit `1e3e123`. |
| **Sincronización del enlace público y del código QR (FIX-06)** | Modelos Gemini en Antigravity | *“El QR debe activarse y cambiar solo cada vez que se reinicie el servidor.”* | Revisión del endpoint `/api/auth/public-url` y prueba con un túnel nuevo. Commit `cd829b2`. |
| **Diseño de las diapositivas y guion por integrante** | Gemini 4 Neon | *“Haz las diapositivas para vender el producto, minimalistas y con imágenes de la página.”* | Selección de capturas reales, ajuste de textos y ensayo de la presentación por cada expositor. |
| **Imágenes ilustrativas para la presentación** | Modelo de generación de imágenes del entorno | *Descripción de deportistas universitarios en un campus con montañas al fondo.* | Uso limitado a dos diapositivas. Son imágenes sintéticas y no representan personas reales; las capturas del presente informe son reales. |
| **Explicación técnica del correo y del asistente** | Gemini 4 Neon | *“Ayúdame a explicar bien cómo funciona el correo y la API de Gemini.”* | Contraste de cada explicación con `EmailService`, `GeminiService` y `AiAssistantController`. |
| **Redacción y revisión de este informe en APA 7** | Gemini 4 Neon y Claude Opus 5.5 | *“Rehaz el documento al objetivo de esta entrega y aplica normas APA 7.ª edición.”* | Revisión de cifras, tablas y códigos de reserva frente a la base de datos, los resultados de Maven y el código fuente. |

*Nota.* Las instrucciones se reproducen de forma resumida. Ningún resultado generado se incorporó sin revisión humana.

### 6.2 Criterios de Uso Responsable

El equipo adoptó cinco criterios para el uso de estas herramientas:

1. **Autoría humana:** Las decisiones de diseño y arquitectura permanecen en el grupo; la herramienta actúa como un asistente de apoyo.
2. **Validación empírica:** Todo cambio de código propuesto se valida con la suite de 36 pruebas y con una prueba manual antes de integrarse.
3. **Trazabilidad en control de versiones:** Los cambios quedan registrados en el historial de Git para mantener la trazabilidad formal del proyecto.
4. **Verificación técnica:** Las afirmaciones técnicas generadas se contrastan con el código fuente antes de presentarse.
5. **Declaración explícita:** El uso de inteligencia artificial se declara de forma explícita y se cita formalmente según las normas APA 7.ª edición (American Psychological Association [APA], 2020).

---

## 7. Conclusiones

La Entrega 3 deja un producto funcional, depurado y validado en condiciones reales. Se aceptaron las 21 historias de usuario, la suite de 36 pruebas automatizadas terminó sin fallos y las seis sesiones con usuarios confirmaron que la plataforma puede usarse desde computadores y celulares con tiempos de reserva inferiores a 8 segundos.

La depuración de errores tuvo un efecto directo en la confiabilidad: la migración a MySQL eliminó la pérdida de datos, el filtro de dominio mantuvo al asistente dentro del servicio, el correo real dio respaldo formal a cada reserva y el rediseño móvil eliminó los problemas de visualización. El despliegue con Cloudflare permite operar la plataforma de inmediato y trasladarla a infraestructura institucional cuando se requiera. Finalmente, el uso declarado de inteligencia artificial generativa resultó útil como apoyo, siempre que cada resultado fuera verificado por el equipo.

---

## Referencias

- American Psychological Association. (2020). *Publication manual of the American Psychological Association* (7th ed.). https://doi.org/10.1037/0000165-000
- Anthropic. (2026). *Claude Opus 5.5* [Modelo de lenguaje de gran tamaño]. https://www.anthropic.com/claude
- Google. (2026). *Google Antigravity* [Plataforma de desarrollo agéntico]. https://antigravity.google
- Google DeepMind. (2026). *Gemini 4 Neon* [Modelo de lenguaje de gran tamaño]. https://deepmind.google/technologies/gemini/
- Pressman, R. S., & Maxim, B. R. (2020). *Software engineering: A practitioner's approach* (9th ed.). McGraw-Hill Education.

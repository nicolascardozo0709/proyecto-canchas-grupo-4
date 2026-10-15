# Guía Completa de Uso: Plataforma de Reserva de Escenarios Deportivos
**Proyecto 3 — Desarrollo de Software | Pontificia Universidad Javeriana**  
**Equipo — Grupo 4:** Nicolás Cardozo Mejía, Samuel Correa, Nicolás Espinosa Soler, Gustavo Mejía

---

## 1. Introducción y Propósito del Proyecto
La **Plataforma de Reserva de Escenarios Deportivos** es un sistema web transaccional moderno diseñado para gestionar de manera integral el catálogo de canchas universitarias (Fútbol 8, Fútbol 11, Tenis de polvo de ladrillo, Pádel panorámico y Coliseo multideportivo).

### Principales Objetivos Cumplidos:
* **Transaccionalidad Anti-Overbooking:** Garantiza el 0% de sobreventa o colisiones de horario gracias a aislamiento transaccional `REPEATABLE READ` y bloqueo pesimista en base de datos.
* **Seguridad RBAC:** Control de acceso estricto por roles (Visitante, Deportista y Administrador) con tokens JWT y encriptación BCrypt.
* **Persistencia Permanente:** Los escenarios, usuarios y reservas se almacenan en disco local bajo formato SQL estándar.
* **Acceso Universal:** Integración de túnel público HTTP/2 seguro para compartir la plataforma en tiempo real con evaluadores o usuarios móviles sin configuraciones de red.

---

## 2. Cómo Iniciar la Plataforma

### Opción A (Recomendada): En 1 Clic con el Script Windows
En la carpeta principal del proyecto, haz doble clic sobre:
```text
iniciar_plataforma.bat
```
Este script realiza automáticamente:
1. Verificación de dependencias y base de datos persistente.
2. Arranque del servidor Spring Boot en el puerto `8080`.
3. Activación del túnel seguro de Cloudflare, imprimiendo en pantalla la URL pública lista para abrir en cualquier celular o computador.

### Opción B: Desde la Terminal (VS Code o PowerShell)
Abre la terminal en la carpeta `backend` y ejecuta:
```powershell
.\mvnw.cmd spring-boot:run
```
Una vez iniciado, abre tu navegador web en:
* **Aplicación Web:** `http://localhost:8080`
* **Consola SQL de Base de Datos:** `http://localhost:8080/h2-console`
* **Documentación de la API (Swagger UI):** `http://localhost:8080/swagger-ui/index.html`

---

## 3. Cuentas de Demostración Precargadas

| Rol | Correo (Email) | Contraseña | Capacidades en el Sistema |
|---|---|---|---|
| **Administrador** | `admin@canchas.com` | `admin123` | Control total: Alternar mantenimiento, editar tarifas, crear canchas y ver todas las reservas. |
| **Deportista Estándar** | `usuario@canchas.com` | `user123` | Búsqueda, reserva de franjas de 60 min, visualización de historial propio y cancelación con liberación inmediata. |
| **Integrantes del Grupo** | `nicolas@canchas.com`<br/>`samuel@canchas.com`<br/>`espinosa@canchas.com`<br/>`gustavo@canchas.com` | `user123` | Cuentas individuales de deportista para pruebas concurrentes. |

---

## 4. Guía de Uso Paso a Paso de la Aplicación Web

### Paso 1: Exploración como Visitante (Sin Iniciar Sesión)
1. Al ingresar a la página web (`http://localhost:8080`), la plataforma arranca en **modo visitante limpio**.
2. **Filtrar por Deporte:** En la barra superior de filtros, haz clic en `Fútbol`, `Tenis`, `Pádel` o `Baloncesto`. El catálogo se actualiza al instante sin recargar la página.
3. **Inspeccionar Escenario:** Cada tarjeta muestra la fotografía oficial, superficie (Sintética, Arcilla, Parquet, Natural), aforo máximo, disponibilidad de iluminación LED y tarifa por hora.
4. Si intentas presionar el botón `[📅 Reservar]` sin estar autenticado, el sistema abrirá amistosamente el modal de inicio de sesión.

---

### Paso 2: Flujo de Reserva como Deportista
1. Haz clic en el botón **`[Iniciar Sesión]`** en la esquina superior derecha.
2. Ingresa con:
   * **Usuario:** `usuario@canchas.com`
   * **Contraseña:** `user123`
3. Observa cómo el menú superior ahora muestra: `Hola, Usuario Deportista` y el botón `[Cerrar Sesión]`.
4. En el catálogo, busca la **Cancha de Pádel Panorámica** y haz clic en **`[📅 Reservar]`**.
5. En la ventana emergente:
   * Selecciona la **Fecha** deseada en el selector de calendario (no se permiten fechas del pasado).
   * Selecciona la **Franja Horaria** deseada (por ejemplo, `18:00 - 19:00`).
   * Haz clic en **`[Confirmar Reserva]`**.
6. El sistema registrará la reserva y te mostrará una alerta verde de confirmación con el código de reserva asignado.

---

### Paso 3: Consultar y Cancelar Reservas Propias
1. En la barra superior de navegación, haz clic en **`[📋 Mis Reservas]`**.
2. Verás una tabla interactiva con todas tus reservas activas, detallando: Cancha, Fecha, Horario, Tarifa pagada y Estado (`CONFIRMADA`).
3. Para cancelar una reserva: haz clic en el botón rojo **`[Cancelar]`** al lado de la reserva.
4. El sistema liberará atómicamente el cupo en la base de datos, permitiendo que cualquier otro deportista pueda volver a tomar esa misma franja.

---

### Paso 4: Demostración de Prevención Anti-Overbooking (Colisión)
Para comprobar que el motor de reservas bloquea intentos duplicados:
1. Abre una ventana de incógnito en tu navegador o usa otro navegador/dispositivo.
2. Inicia sesión con `nicolas@canchas.com` / `user123`.
3. Intenta reservar exactamente la **misma cancha, fecha y hora** que ya fue reservada en el Paso 2.
4. El sistema rechazará la solicitud inmediatamente con una alerta roja:  
   * *"409 Conflict: La franja horaria seleccionada ya se encuentra reservada por otro usuario."*

---

### Paso 5: Operaciones del Perfil Administrador
1. Cierra sesión e inicia sesión con la cuenta administradora:
   * **Usuario:** `admin@canchas.com`
   * **Contraseña:** `admin123`
2. **Transformación de la Interfaz:** Notarás que el catálogo cambia radicalmente respecto al perfil de deportista:
   * Aparece el botón azul **`[+ Nuevo Escenario]`** en la parte superior.
   * En cada cancha aparecen los botones de gestión: **`[Poner en Mantenimiento]`** y **`[✏️ Editar]`**.
3. **Poner en Mantenimiento:**
   * Haz clic en `[Poner en Mantenimiento]` en cualquiera de las canchas.
   * La tarjeta cambiará inmediatamente su etiqueta a color gris: `EN MANTENIMIENTO` y deshabilitará las reservas para proteger a los usuarios de ingresar a un escenario no disponible.
   * Vuelve a presionar el botón `[Habilitar Escenario]` para devolverla al estado operativo.

---

## 5. Inspección de la Base de Datos en Tiempo Real (Consola SQL)

Puedes verificar directamente las inserciones y cambios en la base de datos SQL:

1. Abre en tu navegador: `http://localhost:8080/h2-console`.
2. Verifica los parámetros de conexión:
   * **Driver Class:** `org.h2.Driver`
   * **JDBC URL:** `jdbc:h2:file:./data/canchasdb`
   * **User Name:** `sa`
   * **Password:** *(dejar vacío)*
3. Haz clic en **`Connect`**.
4. En el editor de consultas SQL, ejecuta:
   ```sql
   -- Ver todos los escenarios y su estado de mantenimiento
   SELECT id, nombre, disciplina, superficie, tarifa_hora, en_mantenimiento, imagen_url 
   FROM escenarios;

   -- Ver las reservas confirmadas en tiempo real
   SELECT id, escenario_id, usuario_id, fecha, estado 
   FROM reservas 
   ORDER BY id DESC;

   -- Consultar los usuarios y sus roles asignados
   SELECT id, nombre, apellido, email, rol, activo 
   FROM usuarios;
   ```
Cada acción realizada en la interfaz web (crear usuario, reservar cancha o cambiar a mantenimiento) se refleja de inmediato en estas tablas.

---

## 6. Ejecución de Pruebas Automatizadas (QA)

Para verificar el 100% de éxito en las 19 pruebas del sistema:
1. Abre tu terminal en la carpeta `backend`.
2. Ejecuta:
   ```powershell
   .\mvnw.cmd test
   ```
3. El motor de pruebas ejecutará:
   * **Pruebas Unitarias:** Validación de lógica en `AuthServiceTest`, `EscenarioServiceTest` y `ReservaServiceTest`.
   * **Pruebas de Integración:** Controladores REST y flujos de reserva en `ReservaIntegrationTest`.
   * **Prueba de Concurrencia Multihilo:** `AntiColisionConcurrencyTest`, donde 8 hilos simultáneos disparan una reserva idéntica mediante `CountDownLatch` y el sistema garantiza exactamente 1 reserva confirmada y 7 rechazadas.
4. Resultado esperado: **`BUILD SUCCESS`** sin fallos ni errores.

---

## 7. Estructura de Carpetas del Proyecto

```text
PROYECTO SOFTWARE/
├── backend/                              # Servidor Spring Boot 3
│   ├── src/main/java/com/canchas/reserva/ # Código fuente Java (Controladores, Servicios, Seguridad)
│   ├── src/main/resources/
│   │   ├── static/                       # Frontend SPA (index.html, styles.css, app.js, fotos)
│   │   ├── application.properties        # Configuración persistente H2 en disco
│   │   └── data.sql                      # Datos iniciales del campus
│   ├── src/test/                         # Suite JUnit 5 y configuración de test en memoria
│   └── pom.xml                           # Definición de dependencias Maven
├── docs/                                 # Documentación técnica y académica
│   ├── INFORME_ENTREGA_2_DESARROLLO_Y_PRUEBAS.md
│   ├── GUIA_DE_USO_PLATAFORMA.md         # (Este documento)
│   ├── GUIA_DE_ARQUITECTURA_Y_CODIGO.md
│   ├── MANUAL_DE_INSTALACION_Y_CONFIGURACION.md
│   ├── REPORTE_DE_PRUEBAS_Y_COBERTURA.md
│   └── GITFLOW_Y_PULL_REQUESTS.md
├── Codigo_Definitivo_Proyecto_Canchas_Grupo_4.zip # ZIP oficial de solo código
├── Entrega 2 - Desarrollo y Pruebas - Proyecto 3.pdf # Documento oficial de 15 págs APA 7
├── Guion_Exposicion_8_Minutos_Grupo_4.pdf # Escaleta técnica para la sustentación
├── iniciar_plataforma.bat                # Lanzador de 1 clic para Windows
└── docker-compose.yml                    # Configuración de base de datos MySQL (opcional)
```

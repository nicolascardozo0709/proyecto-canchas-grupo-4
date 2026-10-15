# Manual de Instalación y Configuración
**Proyecto 3: Plataforma de Reserva de Escenarios Deportivos**  
**Asignatura:** Desarrollo de Software (Periodo 2026-3)  
**Institución:** Pontificia Universidad Javeriana - Facultad de Ingeniería  
**Equipo de Trabajo (Grupo 4):**  
- Nicolás Cardozo Mejía (Líder Frontend y Arquitectura)
- Samuel Correa (Líder de Requisitos y DevOps)
- Nicolás Espinosa Soler (Líder Backend y DBA)
- Gustavo Mejía (Líder QA y Product Owner)  
**Fecha de Entrega:** 16 de octubre de 2026

---

## 1. Prerrequisitos del Sistema

Para desplegar y ejecutar la plataforma en cualquier estación de trabajo o servidor, se requieren las siguientes herramientas mínimas:

| Componente | Versión Mínima Recomendada | Propósito |
|---|---|---|
| **Java Development Kit (JDK)** | OpenJDK 21 o superior (compatible con JDK 25) | Entorno de ejecución y compilación de Spring Boot 3 |
| **Apache Maven** | 3.9+ (Se incluye script autónomo `mvnw.cmd`) | Gestor de compilación, empaquetado y dependencias |
| **Node.js y npm** | Node v18+ / npm v9+ | Opcional para desarrollo frontend aislado |
| **Docker y Docker Compose** | Docker v24+ / Compose v2+ | Opcional para levantar MySQL 8 y phpMyAdmin |
| **Navegador Web Moderno** | Google Chrome, Edge o Firefox (versiones recientes) | Interfaz gráfica de usuario y consola de pruebas |

---

## 2. Estructura de Directorios del Proyecto

```text
PROYECTO SOFTWARE/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/canchas/reserva/
│   │   │   │   ├── config/          # Seguridad, CORS, Swagger y Precarga de datos
│   │   │   │   ├── controller/      # Endpoints REST (Auth, Escenarios, Franjas, Reservas)
│   │   │   │   ├── dto/             # Objetos de transferencia de datos con validaciones
│   │   │   │   ├── entity/          # Entidades JPA (Usuario, Escenario, Franja, Reserva)
│   │   │   │   ├── exception/       # Manejador global y excepciones RFC 7807
│   │   │   │   ├── repository/      # Spring Data JPA con bloqueo pesimista
│   │   │   │   ├── security/        # Filtro JWT, UserDetailsService y Tokens
│   │   │   │   └── service/         # Lógica transaccional anti-overbooking
│   │   │   └── resources/
│   │   │       ├── static/          # Frontend Web SPA (index.html, styles.css, app.js)
│   │   │       ├── application.properties         # Perfil H2 (Cero configuración)
│   │   │       ├── application-mysql.properties   # Perfil MySQL 8 para producción
│   │   │       ├── schema.sql                     # DDL de tablas e índices
│   │   │       └── data.sql                       # Datos iniciales (usuarios, canchas)
│   │   └── test/                    # Pruebas Unitarias y de Integración (JUnit 5 + Mockito)
│   ├── pom.xml                      # Descriptores de dependencias Maven
│   └── mvnw.cmd                     # Wrapper local de Maven
├── docs/                            # Documentación técnica, manuales e informe APA 7
├── docker-compose.yml               # Orquestación con MySQL 8 y phpMyAdmin
├── mvnw.cmd                         # Wrapper de Maven en raíz
└── .gitignore
```

---

## 3. Modos de Ejecución de la Plataforma

La aplicación ha sido diseñada con soporte dual de persistencia para garantizar su evaluación inmediata sin fricción técnica:

### Opción A: Modo Autónomo In-Memory (H2 Database) - Recomendado para Calificación Inmediata
No requiere instalar ni configurar motores externos de bases de datos. Todo el backend y frontend se ejecutan de manera instantánea:

1. Abrir una terminal en el directorio raíz del proyecto:
   ```powershell
   cd "c:\Users\nicoc\Downloads\PROYECTO SOFTWARE"
   ```
2. Ejecutar la aplicación con Maven:
   ```cmd
   mvnw.cmd spring-boot:run
   ```
3. Abrir en el navegador:
   - **Aplicación Web Completa (Frontend SPA):** `http://localhost:8080`
   - **Documentación Interactiva Swagger / OpenAPI:** `http://localhost:8080/swagger-ui.html`
   - **Consola de Base de Datos H2:** `http://localhost:8080/h2-console`
     - JDBC URL: `jdbc:h2:mem:canchasdb`
     - Usuario: `sa`
     - Contraseña: *(en blanco)*

---

### Opción B: Modo Producción con Base de Datos MySQL 8 y Docker

1. Iniciar los contenedores de MySQL 8 y phpMyAdmin:
   ```bash
   docker-compose up -d
   ```
2. Verificar que los contenedores estén activos:
   ```bash
   docker ps
   ```
3. Iniciar el backend activando el perfil `mysql`:
   ```cmd
   mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
   ```
4. Accesos adicionales:
   - **phpMyAdmin (Administrador Web de MySQL):** `http://localhost:8081`
     - Servidor: `mysql-db`
     - Usuario: `root`
     - Contraseña: `rootpassword`

---

## 4. Credenciales Preconfiguradas para Pruebas

El sistema inicializa automáticamente las siguientes cuentas con contraseñas cifradas bajo **BCrypt** (factor de costo 12):

| Rol | Correo Electrónico | Contraseña | Permisos y Capacidades |
|---|---|---|---|
| **Administrador** | `admin@javeriana.edu.co` | `admin123` | CRUD de canchas, creación de franjas, alternar estado de mantenimiento (HU-21), ver todas las reservas. |
| **Deportista 1** | `nicolas.cardozo@javeriana.edu.co` | `user123` | Explorar catálogo, consultar matriz de franjas, crear reservas, autogestionar cancelaciones. |
| **Deportista 2** | `samuel.correa@javeriana.edu.co` | `user123` | Crear reservas concurrentes para pruebas de colisión, ver historial propio. |

*Nota: También es posible registrar nuevos usuarios de inmediato desde el formulario web interactivo.*

---

## 5. Ejecución de la Suite de Pruebas Automatizadas

Para validar las pruebas unitarias y de integración:

```cmd
mvnw.cmd test
```

Para generar el empaquetado JAR ejecutable en `backend/target/reserva-canchas-backend-1.0.0-SNAPSHOT.jar`:

```cmd
mvnw.cmd clean package
```

Para ejecutar el JAR compilado directamente:
```cmd
java -jar backend\target\reserva-canchas-backend-1.0.0-SNAPSHOT.jar
```

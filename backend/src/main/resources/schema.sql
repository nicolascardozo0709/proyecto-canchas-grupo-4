-- ==============================================================================
-- DDL - ESQUEMA DE BASE DE DATOS (MYSQL 8 / H2 COMPATIBLE)
-- Proyecto 3: Plataforma de Reserva de Escenarios Deportivos
-- ==============================================================================

-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    rol VARCHAR(30) NOT NULL DEFAULT 'ROLE_USER',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Escenarios Deportivos
CREATE TABLE IF NOT EXISTS escenarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    disciplina VARCHAR(50) NOT NULL,
    superficie VARCHAR(50) NOT NULL,
    aforo INT NOT NULL DEFAULT 10,
    iluminacion BOOLEAN NOT NULL DEFAULT TRUE,
    tarifa_hora DECIMAL(10, 2) NOT NULL DEFAULT 50000.00,
    en_mantenimiento BOOLEAN NOT NULL DEFAULT FALSE,
    descripcion VARCHAR(500),
    imagen_url VARCHAR(255)
);

-- Tabla de Franjas Horarias por Escenario
CREATE TABLE IF NOT EXISTS franjas_horarias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    escenario_id BIGINT NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_franja_escenario FOREIGN KEY (escenario_id) REFERENCES escenarios(id) ON DELETE CASCADE,
    CONSTRAINT uq_franja_escenario_hora UNIQUE (escenario_id, hora_inicio)
);

-- Tabla de Reservas
CREATE TABLE IF NOT EXISTS reservas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo_reserva VARCHAR(40) NOT NULL UNIQUE,
    usuario_id BIGINT NOT NULL,
    escenario_id BIGINT NOT NULL,
    franja_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'CONFIRMADA',
    precio_total DECIMAL(10, 2) NOT NULL,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cancelado_en TIMESTAMP NULL,
    motivo_cancelacion VARCHAR(255) NULL,
    CONSTRAINT fk_reserva_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_reserva_escenario FOREIGN KEY (escenario_id) REFERENCES escenarios(id),
    CONSTRAINT fk_reserva_franja FOREIGN KEY (franja_id) REFERENCES franjas_horarias(id)
);

-- Índices de alto rendimiento para búsqueda y validación anti-colisión
CREATE INDEX IF NOT EXISTS idx_reserva_busqueda ON reservas (escenario_id, franja_id, fecha, estado);
CREATE INDEX IF NOT EXISTS idx_reserva_usuario ON reservas (usuario_id, fecha);

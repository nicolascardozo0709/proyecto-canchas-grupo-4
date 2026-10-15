-- ==============================================================================
-- DML - DATOS DE SEMILLA (SEED DATA)
-- Proyecto 3: Plataforma de Reserva de Escenarios Deportivos
-- ==============================================================================

-- Usuarios del Sistema (Contraseñas hasheadas con BCrypt cost 12: 'admin123' y 'user123')
-- $2a$12$K8yQd1gL3r1o8d9.qCqCae2M7qN7o.oT6P.R7FjF0o2N3B4P5yK5O -> 'admin123' / 'user123'
INSERT INTO usuarios (id, nombre, apellido, email, password, telefono, rol, activo) VALUES
(1, 'Administrador', 'Javeriana', 'admin@javeriana.edu.co', '$2a$12$y16Z3C3G8p2f/V1nE9Z7n.E0H.jYhI0J6a5Q4K9q9bY.T0w3L6D.G', '3001234567', 'ROLE_ADMIN', TRUE),
(2, 'Nicolás', 'Cardozo Mejía', 'nicolas.cardozo@javeriana.edu.co', '$2a$12$y16Z3C3G8p2f/V1nE9Z7n.E0H.jYhI0J6a5Q4K9q9bY.T0w3L6D.G', '3109876543', 'ROLE_USER', TRUE),
(3, 'Samuel', 'Correa', 'samuel.correa@javeriana.edu.co', '$2a$12$y16Z3C3G8p2f/V1nE9Z7n.E0H.jYhI0J6a5Q4K9q9bY.T0w3L6D.G', '3205551234', 'ROLE_USER', TRUE),
(4, 'Nicolás', 'Espinosa Soler', 'nicolas.espinosa@javeriana.edu.co', '$2a$12$y16Z3C3G8p2f/V1nE9Z7n.E0H.jYhI0J6a5Q4K9q9bY.T0w3L6D.G', '3114445566', 'ROLE_USER', TRUE),
(5, 'Gustavo', 'Mejía', 'gustavo.mejia@javeriana.edu.co', '$2a$12$y16Z3C3G8p2f/V1nE9Z7n.E0H.jYhI0J6a5Q4K9q9bY.T0w3L6D.G', '3158889900', 'ROLE_USER', TRUE);

-- Escenarios Deportivos (Canchas)
INSERT INTO escenarios (id, nombre, disciplina, superficie, aforo, iluminacion, tarifa_hora, en_mantenimiento, descripcion, imagen_url) VALUES
(1, 'Cancha Sintética 1 - El Campincito', 'FUTBOL', 'SINTETICA', 16, TRUE, 85000.00, FALSE, 'Cancha de fútbol 8 con grama sintética de última generación e iluminación LED para partidos nocturnos.', 'https://images.unsplash.com/photo-1529900748604-07564a03e7a6?w=800&auto=format&fit=crop'),
(2, 'Pista Central de Tenis', 'TENIS', 'ARCILLA', 4, TRUE, 60000.00, FALSE, 'Cancha reglamentaria en polvo de ladrillo (arcilla roja), drenaje rápido y gradería.', 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=800&auto=format&fit=crop'),
(3, 'Cancha de Pádel Panorámica', 'PADEL', 'SINTETICA', 4, TRUE, 70000.00, FALSE, 'Pista de pádel con cerramiento de vidrio panorámico templado y césped monofilamento azul.', 'https://images.unsplash.com/photo-1612872087720-bb876e2e67d1?w=800&auto=format&fit=crop'),
(4, 'Coliseo de Baloncesto y Voleibol', 'BALONCESTO', 'PARQUET', 20, TRUE, 90000.00, FALSE, 'Tablón de madera parquet de alto impacto con aros retráctiles y demarcación multideportiva.', 'https://images.unsplash.com/photo-1546519638-68e109498ffc?w=800&auto=format&fit=crop'),
(5, 'Cancha de Fútbol 11 - La Javeriana', 'FUTBOL', 'NATURAL', 22, TRUE, 120000.00, FALSE, 'Campo reglamentario de grama natural bermuda con sistema de riego tecnificado.', 'https://images.unsplash.com/photo-1459865264687-595d652de67e?w=800&auto=format&fit=crop');

-- Franjas Horarias Parametrizadas (Bloques de 60 minutos) para Escenario 1
INSERT INTO franjas_horarias (escenario_id, hora_inicio, hora_fin, activa) VALUES
(1, '06:00:00', '07:00:00', TRUE),
(1, '07:00:00', '08:00:00', TRUE),
(1, '08:00:00', '09:00:00', TRUE),
(1, '09:00:00', '10:00:00', TRUE),
(1, '10:00:00', '11:00:00', TRUE),
(1, '16:00:00', '17:00:00', TRUE),
(1, '17:00:00', '18:00:00', TRUE),
(1, '18:00:00', '19:00:00', TRUE),
(1, '19:00:00', '20:00:00', TRUE),
(1, '20:00:00', '21:00:00', TRUE),
(1, '21:00:00', '22:00:00', TRUE);

-- Franjas Horarias para Escenario 2 (Tenis)
INSERT INTO franjas_horarias (escenario_id, hora_inicio, hora_fin, activa) VALUES
(2, '07:00:00', '08:00:00', TRUE),
(2, '08:00:00', '09:00:00', TRUE),
(2, '09:00:00', '10:00:00', TRUE),
(2, '16:00:00', '17:00:00', TRUE),
(2, '17:00:00', '18:00:00', TRUE),
(2, '18:00:00', '19:00:00', TRUE);

-- Franjas Horarias para Escenario 3 (Pádel)
INSERT INTO franjas_horarias (escenario_id, hora_inicio, hora_fin, activa) VALUES
(3, '08:00:00', '09:00:00', TRUE),
(3, '09:00:00', '10:00:00', TRUE),
(3, '17:00:00', '18:00:00', TRUE),
(3, '18:00:00', '19:00:00', TRUE),
(3, '19:00:00', '20:00:00', TRUE),
(3, '20:00:00', '21:00:00', TRUE);

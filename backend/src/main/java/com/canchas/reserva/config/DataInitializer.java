package com.canchas.reserva.config;

import com.canchas.reserva.entity.*;
import com.canchas.reserva.repository.EscenarioRepository;
import com.canchas.reserva.repository.FranjaHorariaRepository;
import com.canchas.reserva.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final EscenarioRepository escenarioRepository;
    private final FranjaHorariaRepository franjaRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           EscenarioRepository escenarioRepository,
                           FranjaHorariaRepository franjaRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.escenarioRepository = escenarioRepository;
        this.franjaRepository = franjaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Inicializar Usuarios si no existen
        if (usuarioRepository.count() == 0) {
            Usuario admin = new Usuario(
                "Administrador",
                "Javeriana",
                "admin@javeriana.edu.co",
                passwordEncoder.encode("admin123"),
                "3001234567",
                Rol.ROLE_ADMIN
            );
            usuarioRepository.save(admin);

            Usuario user1 = new Usuario(
                "Nicolás",
                "Cardozo",
                "nicolas.cardozo@javeriana.edu.co",
                passwordEncoder.encode("user123"),
                "3109876543",
                Rol.ROLE_USER
            );
            usuarioRepository.save(user1);

            Usuario user2 = new Usuario(
                "Samuel",
                "Correa",
                "samuel.correa@javeriana.edu.co",
                passwordEncoder.encode("user123"),
                "3205551234",
                Rol.ROLE_USER
            );
            usuarioRepository.save(user2);
        }

        // Inicializar Escenarios y Franjas Horarias si no existen
        if (escenarioRepository.count() == 0) {
            Escenario futbol8 = new Escenario(
                "Cancha Sintética 1 - El Campincito",
                Disciplina.FUTBOL,
                Superficie.SINTETICA,
                16,
                true,
                BigDecimal.valueOf(85000.00),
                false,
                "Cancha de fútbol 8 con grama sintética de última generación e iluminación LED para partidos nocturnos.",
                "https://images.unsplash.com/photo-1529900748604-07564a03e7a6?w=800&auto=format&fit=crop"
            );
            futbol8 = escenarioRepository.save(futbol8);
            crearFranjasEstandar(futbol8, 6, 22);

            Escenario tenis = new Escenario(
                "Pista Central de Tenis",
                Disciplina.TENIS,
                Superficie.ARCILLA,
                4,
                true,
                BigDecimal.valueOf(60000.00),
                false,
                "Cancha reglamentaria en polvo de ladrillo (arcilla roja), drenaje rápido y gradería lateral.",
                "https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?w=800&auto=format&fit=crop"
            );
            tenis = escenarioRepository.save(tenis);
            crearFranjasEstandar(tenis, 6, 20);

            Escenario padel = new Escenario(
                "Cancha de Pádel Panorámica",
                Disciplina.PADEL,
                Superficie.SINTETICA,
                4,
                true,
                BigDecimal.valueOf(70000.00),
                false,
                "Pista de pádel con cerramiento de vidrio templado panorámico y césped monofilamento azul.",
                "https://images.unsplash.com/photo-1554068865-24cecd4e34b8?w=800&auto=format&fit=crop"
            );
            padel = escenarioRepository.save(padel);
            crearFranjasEstandar(padel, 7, 22);

            Escenario coliseo = new Escenario(
                "Coliseo de Baloncesto y Voleibol",
                Disciplina.BALONCESTO,
                Superficie.PARQUET,
                20,
                true,
                BigDecimal.valueOf(90000.00),
                false,
                "Tablón de madera parquet de alto impacto con tableros de cristal retráctiles y sonido ambiental.",
                "https://images.unsplash.com/photo-1546519638-68e109498ffc?w=800&auto=format&fit=crop"
            );
            coliseo = escenarioRepository.save(coliseo);
            crearFranjasEstandar(coliseo, 8, 21);

            Escenario futbol11 = new Escenario(
                "Cancha de Fútbol 11 - La Javeriana",
                Disciplina.FUTBOL,
                Superficie.NATURAL,
                22,
                true,
                BigDecimal.valueOf(130000.00),
                false,
                "Campo reglamentario de grama natural bermuda con sistema de drenaje tecnificado.",
                "https://images.unsplash.com/photo-1459865264687-595d652de67e?w=800&auto=format&fit=crop"
            );
            futbol11 = escenarioRepository.save(futbol11);
            crearFranjasEstandar(futbol11, 7, 19);
        }
    }

    private void crearFranjasEstandar(Escenario escenario, int horaInicio, int horaFin) {
        for (int h = horaInicio; h < horaFin; h++) {
            LocalTime inicio = LocalTime.of(h, 0);
            LocalTime fin = LocalTime.of(h + 1, 0);
            FranjaHoraria franja = new FranjaHoraria(escenario, inicio, fin, true);
            franjaRepository.save(franja);
        }
    }
}

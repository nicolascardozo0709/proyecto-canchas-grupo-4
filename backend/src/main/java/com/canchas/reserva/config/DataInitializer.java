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

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final EscenarioRepository escenarioRepository;
    private final FranjaHorariaRepository franjaRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.core.env.Environment environment;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           EscenarioRepository escenarioRepository,
                           FranjaHorariaRepository franjaRepository,
                           PasswordEncoder passwordEncoder,
                           org.springframework.core.env.Environment environment) {
        this.usuarioRepository = usuarioRepository;
        this.escenarioRepository = escenarioRepository;
        this.franjaRepository = franjaRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    public void run(String... args) {
        if (environment.acceptsProfiles(org.springframework.core.env.Profiles.of("prod"))) {
            return;
        }

        crearUsuarioSiNoExiste("Administrador", "Sistema", "admin@canchas.com", "admin123", "3001234567", Rol.ROLE_ADMIN);
        crearUsuarioSiNoExiste("Usuario", "Deportista", "usuario@canchas.com", "user123", "3109876543", Rol.ROLE_USER);
        crearUsuarioSiNoExiste("Nicolás", "Cardozo", "nicolas@canchas.com", "user123", "3109876543", Rol.ROLE_USER);
        crearUsuarioSiNoExiste("Samuel", "Correa", "samuel@canchas.com", "user123", "3205551234", Rol.ROLE_USER);
        crearUsuarioSiNoExiste("Nicolás", "Espinosa", "espinosa@canchas.com", "user123", "3114445566", Rol.ROLE_USER);
        crearUsuarioSiNoExiste("Gustavo", "Mejía", "gustavo@canchas.com", "user123", "3158889900", Rol.ROLE_USER);

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
                "/futbol8.jpg"
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
                "/tenis.jpg"
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
                "/padel.png"
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
                "/coliseo.jpg"
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
                "/futbol11.jpg"
            );
            futbol11 = escenarioRepository.save(futbol11);
            crearFranjasEstandar(futbol11, 7, 19);
        }

        // Sincronizar imágenes locales de alta resolución en todos los escenarios existentes
        escenarioRepository.findAll().forEach(e -> {
            String nuevaImg = null;
            if (e.getDisciplina() == Disciplina.PADEL) nuevaImg = "/padel.png";
            else if (e.getDisciplina() == Disciplina.TENIS) nuevaImg = "/tenis.jpg";
            else if (e.getDisciplina() == Disciplina.BALONCESTO) nuevaImg = "/coliseo.jpg";
            else if (e.getDisciplina() == Disciplina.FUTBOL) {
                nuevaImg = (e.getNombre() != null && e.getNombre().contains("11")) ? "/futbol11.jpg" : "/futbol8.jpg";
            }
            if (nuevaImg != null && !nuevaImg.equals(e.getImagenUrl())) {
                e.setImagenUrl(nuevaImg);
                escenarioRepository.save(e);
            }
        });
    }

    private void crearUsuarioSiNoExiste(String nombre, String apellido, String email, String password, String telefono, Rol rol) {
        if (!usuarioRepository.existsByEmail(email)) {
            Usuario u = new Usuario(nombre, apellido, email, passwordEncoder.encode(password), telefono, rol);
            usuarioRepository.save(u);
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

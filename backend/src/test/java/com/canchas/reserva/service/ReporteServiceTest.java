package com.canchas.reserva.service;

import com.canchas.reserva.dto.ReporteDtos.*;
import com.canchas.reserva.entity.*;
import com.canchas.reserva.exception.BusinessException;
import com.canchas.reserva.repository.EscenarioRepository;
import com.canchas.reserva.repository.FranjaHorariaRepository;
import com.canchas.reserva.repository.ReservaRepository;
import com.canchas.reserva.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del módulo de datos (Entrega 3): ocupación, demanda por día/hora y CSV.
 * Se usa un conjunto de datos pequeño y determinista para poder verificar los cálculos a mano.
 */
@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    // Semana fija: lunes 2026-11-02 ... domingo 2026-11-08 (7 días)
    private static final LocalDate LUNES = LocalDate.of(2026, 11, 2);
    private static final LocalDate DOMINGO = LocalDate.of(2026, 11, 8);

    @Mock private ReservaRepository reservaRepository;
    @Mock private EscenarioRepository escenarioRepository;
    @Mock private FranjaHorariaRepository franjaRepository;
    @Mock private UsuarioRepository usuarioRepository;

    private ReporteService reporteService;

    private Usuario usuario;
    private Escenario futbol;
    private Escenario tenis;
    private FranjaHoraria futbol8, futbol9, tenis8;

    @BeforeEach
    void setUp() {
        ReservaService reservaService = new ReservaService(reservaRepository, escenarioRepository, franjaRepository, usuarioRepository);
        reporteService = new ReporteService(reservaRepository, escenarioRepository, franjaRepository, usuarioRepository, reservaService);

        usuario = new Usuario("Nicolás", "Cardozo", "nicolas@canchas.com", "hash", "300", Rol.ROLE_USER);
        usuario.setId(10L);

        futbol = new Escenario("Cancha Sintética", Disciplina.FUTBOL, Superficie.SINTETICA, 16, true,
            BigDecimal.valueOf(85000), false, "d", "i.jpg");
        futbol.setId(1L);
        tenis = new Escenario("Court de Tenis", Disciplina.TENIS, Superficie.ARCILLA, 4, true,
            BigDecimal.valueOf(60000), false, "d", "i.jpg");
        tenis.setId(2L);

        futbol8 = franja(11L, futbol, 8);
        futbol9 = franja(12L, futbol, 9);
        tenis8 = franja(21L, tenis, 8);
    }

    private FranjaHoraria franja(Long id, Escenario esc, int hora) {
        FranjaHoraria f = new FranjaHoraria(esc, LocalTime.of(hora, 0), LocalTime.of(hora + 1, 0), true);
        f.setId(id);
        return f;
    }

    private Reserva reserva(String codigo, Escenario esc, FranjaHoraria fr, LocalDate fecha, double precio) {
        return new Reserva(codigo, usuario, esc, fr, fecha, BigDecimal.valueOf(precio));
    }

    private void stubDatosDeLaSemana() {
        Reserva cancelada = reserva("RES-4", futbol, futbol8, LUNES.plusDays(2), 85000);
        cancelada.setEstado(EstadoReserva.CANCELADA);

        List<Reserva> todas = List.of(
            reserva("RES-1", futbol, futbol8, LUNES, 85000),                 // lunes
            reserva("RES-2", futbol, futbol8, LUNES.plusDays(1), 85000),     // martes
            reserva("RES-3", futbol, futbol9, LUNES, 85000),                 // lunes
            reserva("RES-5", tenis, tenis8, LUNES, 60000),                   // lunes
            cancelada                                                        // miércoles, cancelada
        );
        when(reservaRepository.findAll(ArgumentMatchers.<Specification<Reserva>>any())).thenReturn(new ArrayList<>(todas));
        when(escenarioRepository.findAll()).thenReturn(List.of(futbol, tenis));
        when(franjaRepository.findByEscenarioIdAndActivaTrueOrderByHoraInicioAsc(1L)).thenReturn(List.of(futbol8, futbol9));
        when(franjaRepository.findByEscenarioIdAndActivaTrueOrderByHoraInicioAsc(2L)).thenReturn(List.of(tenis8));
    }

    @Test
    @DisplayName("Estadísticas: ocupación por escenario = reservas vigentes / (franjas activas x días)")
    void ocupacionPorEscenario() {
        stubDatosDeLaSemana();

        EstadisticasResponse r = reporteService.estadisticas(LUNES, DOMINGO, null);

        assertThat(r.diasRango()).isEqualTo(7);
        assertThat(r.totalVigentes()).isEqualTo(4);
        assertThat(r.totalCanceladas()).isEqualTo(1);
        assertThat(r.ingresos()).isEqualByComparingTo("315000");

        // Fútbol: 3 reservas / (2 franjas x 7 días = 14) = 21,4 %.  Tenis: 1 / 7 = 14,3 %
        assertThat(r.porEscenario()).hasSize(2);
        OcupacionEscenario primero = r.porEscenario().get(0);
        assertThat(primero.escenarioNombre()).isEqualTo("Cancha Sintética");   // ordenado por ocupación desc
        assertThat(primero.reservas()).isEqualTo(3);
        assertThat(primero.franjasPosibles()).isEqualTo(14);
        assertThat(primero.ocupacionPct()).isEqualTo(21.4);
        assertThat(primero.ingresos()).isEqualByComparingTo("255000");
        assertThat(r.porEscenario().get(1).ocupacionPct()).isEqualTo(14.3);

        // Global: 4 / 21 = 19,0 %
        assertThat(r.ocupacionGlobalPct()).isEqualTo(19.0);
    }

    @Test
    @DisplayName("Estadísticas: ocupación por franja agrega todos los escenarios que ofrecen esa hora")
    void ocupacionPorFranja() {
        stubDatosDeLaSemana();

        EstadisticasResponse r = reporteService.estadisticas(LUNES, DOMINGO, null);

        assertThat(r.porFranja()).hasSize(2);
        OcupacionFranja ocho = r.porFranja().get(0);
        assertThat(ocho.horaInicio()).isEqualTo("08:00");
        assertThat(ocho.horaFin()).isEqualTo("09:00");
        assertThat(ocho.reservas()).isEqualTo(3);            // fútbol lun, fútbol mar, tenis lun
        assertThat(ocho.franjasPosibles()).isEqualTo(14);    // 2 escenarios x 7 días
        assertThat(ocho.ocupacionPct()).isEqualTo(21.4);

        OcupacionFranja nueve = r.porFranja().get(1);
        assertThat(nueve.horaInicio()).isEqualTo("09:00");
        assertThat(nueve.reservas()).isEqualTo(1);
        assertThat(nueve.franjasPosibles()).isEqualTo(7);    // solo fútbol ofrece las 09:00
        assertThat(nueve.ocupacionPct()).isEqualTo(14.3);
    }

    @Test
    @DisplayName("Estadísticas: demanda por día de la semana, día pico y hora pico (las canceladas no cuentan)")
    void demandaPorDiaYHora() {
        stubDatosDeLaSemana();

        EstadisticasResponse r = reporteService.estadisticas(LUNES, DOMINGO, null);

        assertThat(r.porDiaSemana()).hasSize(7);
        assertThat(r.porDiaSemana().get(0).dia()).isEqualTo("Lunes");
        assertThat(r.porDiaSemana().get(0).reservas()).isEqualTo(3);
        assertThat(r.porDiaSemana().get(1).reservas()).isEqualTo(1);   // martes
        assertThat(r.porDiaSemana().get(2).reservas()).isZero();       // miércoles: solo había una cancelada
        assertThat(r.diaPico()).isEqualTo("Lunes");
        assertThat(r.horaPico()).isEqualTo("08:00 - 09:00");
    }

    @Test
    @DisplayName("Estadísticas sin datos: no divide por cero y usa los últimos 30 días")
    void sinDatos() {
        when(reservaRepository.findAll(ArgumentMatchers.<Specification<Reserva>>any())).thenReturn(List.of());
        when(escenarioRepository.findAll()).thenReturn(List.of());

        EstadisticasResponse r = reporteService.estadisticas(null, null, null);

        assertThat(r.diasRango()).isEqualTo(30);
        assertThat(r.totalVigentes()).isZero();
        assertThat(r.ocupacionGlobalPct()).isEqualTo(0.0);
        assertThat(r.diaPico()).isNull();
        assertThat(r.horaPico()).isNull();
        assertThat(r.porDiaSemana()).hasSize(7);
    }

    @Test
    @DisplayName("Rango de fechas invertido (desde > hasta) se rechaza con BusinessException")
    void rangoInvertido() {
        assertThatThrownBy(() -> reporteService.estadisticas(DOMINGO, LUNES, null))
            .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> reporteService.filtrar(null, null, DOMINGO, LUNES, null))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("CSV: encabezado, fila de datos y separador configurable")
    void csvBasico() {
        when(reservaRepository.findAll(ArgumentMatchers.<Specification<Reserva>>any()))
            .thenReturn(List.of(reserva("RES-1", futbol, futbol8, LUNES, 85000)));

        String csv = reporteService.exportarCsv(null, null, LUNES, DOMINGO, null, ';');
        String[] lineas = csv.split("\r\n");

        assertThat(lineas).hasSize(2);
        assertThat(lineas[0]).startsWith("Código;Deportista;Correo;Escenario;Disciplina;Fecha;Hora inicio;Hora fin;Estado;Precio total (COP)");
        assertThat(lineas[1]).startsWith("RES-1;Nicolás Cardozo;nicolas@canchas.com;Cancha Sintética;FUTBOL;2026-11-02;08:00;09:00;CONFIRMADA;85000;");
    }

    @Test
    @DisplayName("CSV: separador no soportado se rechaza")
    void csvSeparadorInvalido() {
        assertThatThrownBy(() -> reporteService.exportarCsv(null, null, null, null, null, '|'))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("CSV: escapa comillas, separadores y saltos de línea")
    void csvEscapado() {
        assertThat(ReporteService.celdaCsv("Cancha, grande", ',')).isEqualTo("\"Cancha, grande\"");
        assertThat(ReporteService.celdaCsv("Dijo \"hola\"", ',')).isEqualTo("\"Dijo \"\"hola\"\"\"");
        assertThat(ReporteService.celdaCsv("línea1\nlínea2", ';')).isEqualTo("\"línea1\nlínea2\"");
        // la coma no obliga a comillas cuando el separador es ';'
        assertThat(ReporteService.celdaCsv("Cancha, grande", ';')).isEqualTo("Cancha, grande");
        assertThat(ReporteService.celdaCsv(null, ',')).isEmpty();
    }

    @Test
    @DisplayName("CSV: neutraliza inyección de fórmulas en textos controlados por el usuario")
    void csvInyeccionDeFormulas() {
        assertThat(ReporteService.celdaCsv("=HYPERLINK(\"http://malo\")", ';')).startsWith("\"'=HYPERLINK");
        assertThat(ReporteService.celdaCsv("+1+1", ',')).isEqualTo("'+1+1");
        assertThat(ReporteService.celdaCsv("-2+3", ',')).isEqualTo("'-2+3");
        assertThat(ReporteService.celdaCsv("@SUM(A1)", ',')).isEqualTo("'@SUM(A1)");
        assertThat(ReporteService.celdaCsv("Nicolás", ',')).isEqualTo("Nicolás");   // texto normal no se altera
    }

    @Test
    @DisplayName("Porcentaje: redondeo a un decimal y capacidad cero devuelve 0")
    void porcentaje() {
        assertThat(ReporteService.porcentaje(1, 3)).isEqualTo(33.3);
        assertThat(ReporteService.porcentaje(2, 3)).isEqualTo(66.7);
        assertThat(ReporteService.porcentaje(5, 0)).isEqualTo(0.0);
        assertThat(ReporteService.porcentaje(10, 10)).isEqualTo(100.0);
    }
}

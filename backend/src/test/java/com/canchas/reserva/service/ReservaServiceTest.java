package com.canchas.reserva.service;

import com.canchas.reserva.dto.ReservaDtos.*;
import com.canchas.reserva.entity.*;
import com.canchas.reserva.exception.BusinessException;
import com.canchas.reserva.exception.CollisionException;
import com.canchas.reserva.exception.UnauthorizedAccessException;
import com.canchas.reserva.repository.EscenarioRepository;
import com.canchas.reserva.repository.FranjaHorariaRepository;
import com.canchas.reserva.repository.ReservaRepository;
import com.canchas.reserva.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private EscenarioRepository escenarioRepository;

    @Mock
    private FranjaHorariaRepository franjaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ReservaService reservaService;

    private Usuario usuarioMock;
    private Escenario escenarioMock;
    private FranjaHoraria franjaMock;

    @BeforeEach
    void setUp() {
        usuarioMock = new Usuario("Nicolás", "Cardozo", "nicolas@javeriana.edu.co", "hash", "300123", Rol.ROLE_USER);
        usuarioMock.setId(10L);

        escenarioMock = new Escenario(
            "Cancha Sintética 1",
            Disciplina.FUTBOL,
            Superficie.SINTETICA,
            16,
            true,
            BigDecimal.valueOf(85000.00),
            false,
            "Cancha sintética",
            "img.jpg"
        );
        escenarioMock.setId(1L);

        franjaMock = new FranjaHoraria(escenarioMock, LocalTime.of(18, 0), LocalTime.of(19, 0), true);
        franjaMock.setId(100L);
    }

    @Test
    @DisplayName("HU-05: Debe crear una reserva exitosamente cuando la franja y fecha son válidas")
    void crearReserva_Exito() {
        LocalDate fechaFutura = LocalDate.now().plusDays(2);
        ReservaRequest request = new ReservaRequest(1L, 100L, fechaFutura);

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuarioMock));
        when(escenarioRepository.findById(1L)).thenReturn(Optional.of(escenarioMock));
        when(franjaRepository.findByIdWithLock(100L)).thenReturn(Optional.of(franjaMock));
        when(reservaRepository.findActiveReservaWithLock(eq(1L), eq(100L), eq(fechaFutura), eq(EstadoReserva.CONFIRMADA)))
            .thenReturn(Optional.empty());

        Reserva reservaGuardada = new Reserva("RES-TEST", usuarioMock, escenarioMock, franjaMock, fechaFutura, BigDecimal.valueOf(85000.00));
        reservaGuardada.setId(500L);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaGuardada);

        ReservaResponse response = reservaService.crearReserva(10L, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(500L);
        assertThat(response.getCodigoReserva()).isEqualTo("RES-TEST");
        assertThat(response.getEstado()).isEqualTo(EstadoReserva.CONFIRMADA);
        assertThat(response.getPrecioTotal()).isEqualByComparingTo("85000.00");
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    @DisplayName("HU-08: Debe rechazar reservas en fechas pasadas")
    void crearReserva_FechaPasada_LanzaExcepcion() {
        LocalDate fechaPasada = LocalDate.now().minusDays(1);
        ReservaRequest request = new ReservaRequest(1L, 100L, fechaPasada);

        assertThatThrownBy(() -> reservaService.crearReserva(10L, request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("No se permiten reservas en fechas pasadas");

        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("HU-21: Debe rechazar reservas si el escenario deportivo está en mantenimiento")
    void crearReserva_EscenarioEnMantenimiento_LanzaExcepcion() {
        LocalDate fechaFutura = LocalDate.now().plusDays(1);
        escenarioMock.setEnMantenimiento(true); // En mantenimiento
        ReservaRequest request = new ReservaRequest(1L, 100L, fechaFutura);

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuarioMock));
        when(escenarioRepository.findById(1L)).thenReturn(Optional.of(escenarioMock));

        assertThatThrownBy(() -> reservaService.crearReserva(10L, request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("mantenimiento");

        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("HU-07 & RNF-02: Debe lanzar CollisionException si la franja ya está ocupada en esa fecha (Anti-Overbooking)")
    void crearReserva_ColisionOverbooking_LanzaExcepcion() {
        LocalDate fechaFutura = LocalDate.now().plusDays(3);
        ReservaRequest request = new ReservaRequest(1L, 100L, fechaFutura);

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuarioMock));
        when(escenarioRepository.findById(1L)).thenReturn(Optional.of(escenarioMock));
        when(franjaRepository.findByIdWithLock(100L)).thenReturn(Optional.of(franjaMock));

        // Simular que ya existe una reserva activa para esa misma franja y fecha
        Reserva reservaExistente = new Reserva("RES-OCUPADA", usuarioMock, escenarioMock, franjaMock, fechaFutura, BigDecimal.valueOf(85000.00));
        when(reservaRepository.findActiveReservaWithLock(eq(1L), eq(100L), eq(fechaFutura), eq(EstadoReserva.CONFIRMADA)))
            .thenReturn(Optional.of(reservaExistente));

        assertThatThrownBy(() -> reservaService.crearReserva(10L, request))
            .isInstanceOf(CollisionException.class)
            .hasMessageContaining("ya se encuentra ocupada");

        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("HU-06: Debe cancelar la reserva y liberar la franja horaria inmediatamente")
    void cancelarReserva_Exito_LiberaFranja() {
        LocalDate fecha = LocalDate.now().plusDays(2);
        Reserva reserva = new Reserva("RES-ABC", usuarioMock, escenarioMock, franjaMock, fecha, BigDecimal.valueOf(85000.00));
        reserva.setId(200L);

        when(reservaRepository.findById(200L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));

        ReservaResponse response = reservaService.cancelarReserva(200L, 10L, false, "Imprevisto laboral");

        assertThat(response).isNotNull();
        assertThat(response.getEstado()).isEqualTo(EstadoReserva.CANCELADA);
        assertThat(response.getCanceladoEn()).isNotNull();
        assertThat(response.getMotivoCancelacion()).isEqualTo("Imprevisto laboral");
        verify(reservaRepository).save(reserva);
    }

    @Test
    @DisplayName("HU-19: Un usuario regular no puede cancelar reservas pertenecientes a otro usuario")
    void cancelarReserva_UsuarioAjeno_LanzaUnauthorized() {
        LocalDate fecha = LocalDate.now().plusDays(2);
        Usuario otroUsuario = new Usuario("Carlos", "Gómez", "carlos@javeriana.edu.co", "hash", "311111", Rol.ROLE_USER);
        otroUsuario.setId(99L);

        Reserva reservaDeCarlos = new Reserva("RES-CARLOS", otroUsuario, escenarioMock, franjaMock, fecha, BigDecimal.valueOf(85000.00));
        reservaDeCarlos.setId(300L);

        when(reservaRepository.findById(300L)).thenReturn(Optional.of(reservaDeCarlos));

        // El usuario 10 intenta cancelar la reserva del usuario 99 sin ser admin
        assertThatThrownBy(() -> reservaService.cancelarReserva(300L, 10L, false, "Cancelar"))
            .isInstanceOf(UnauthorizedAccessException.class)
            .hasMessageContaining("Acceso denegado");

        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    @DisplayName("WF-02: Debe consultar disponibilidad horaria clasificando franjas libres y ocupadas")
    void consultarDisponibilidad_IdentificaFranjaOcupadaYLibre() {
        LocalDate fecha = LocalDate.now().plusDays(1);
        FranjaHoraria franja2 = new FranjaHoraria(escenarioMock, LocalTime.of(19, 0), LocalTime.of(20, 0), true);
        franja2.setId(101L);

        when(escenarioRepository.existsById(1L)).thenReturn(true);
        when(franjaRepository.findByEscenarioIdAndActivaTrueOrderByHoraInicioAsc(1L))
            .thenReturn(List.of(franjaMock, franja2));

        // Supongamos que la franja 100 está reservada, y la 101 está libre
        Reserva reservaOcupada = new Reserva("RES-OCU", usuarioMock, escenarioMock, franjaMock, fecha, BigDecimal.valueOf(85000.00));
        reservaOcupada.setId(888L);

        when(reservaRepository.findByEscenarioIdAndFechaAndEstado(1L, fecha, EstadoReserva.CONFIRMADA))
            .thenReturn(List.of(reservaOcupada));

        List<DisponibilidadFranjaResponse> disponibilidad = reservaService.consultarDisponibilidad(1L, fecha);

        assertThat(disponibilidad).hasSize(2);

        // Franja 100 ocupada
        DisponibilidadFranjaResponse disp1 = disponibilidad.get(0);
        assertThat(disp1.getFranjaId()).isEqualTo(100L);
        assertThat(disp1.isDisponible()).isFalse();
        assertThat(disp1.getReservaId()).isEqualTo(888L);

        // Franja 101 libre
        DisponibilidadFranjaResponse disp2 = disponibilidad.get(1);
        assertThat(disp2.getFranjaId()).isEqualTo(101L);
        assertThat(disp2.isDisponible()).isTrue();
        assertThat(disp2.getReservaId()).isNull();
    }
}

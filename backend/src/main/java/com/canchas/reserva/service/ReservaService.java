package com.canchas.reserva.service;

import com.canchas.reserva.dto.ReservaDtos.*;
import com.canchas.reserva.entity.*;
import com.canchas.reserva.exception.BusinessException;
import com.canchas.reserva.exception.CollisionException;
import com.canchas.reserva.exception.ResourceNotFoundException;
import com.canchas.reserva.exception.UnauthorizedAccessException;
import com.canchas.reserva.repository.EscenarioRepository;
import com.canchas.reserva.repository.FranjaHorariaRepository;
import com.canchas.reserva.repository.ReservaRepository;
import com.canchas.reserva.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio transaccional central para la gestión de reservas de escenarios deportivos.
 * Implementa las reglas de negocio críticas:
 * - HU-05: Creación de reservas usuario-franja-fecha.
 * - HU-06: Cancelación de reservas y liberación atómica de la franja.
 * - HU-07: Validación anti-overbooking concurrente con aislamiento transaccional y bloqueo pesimista.
 * - HU-08: Bloqueo de reservas en fechas pasadas.
 * - HU-15 / HU-19: Autogestión y aislamiento de reservas por usuario.
 * - HU-21: Bloqueo de reservas en escenarios marcados en mantenimiento.
 */
@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final EscenarioRepository escenarioRepository;
    private final FranjaHorariaRepository franjaRepository;
    private final UsuarioRepository usuarioRepository;

    public ReservaService(ReservaRepository reservaRepository,
                          EscenarioRepository escenarioRepository,
                          FranjaHorariaRepository franjaRepository,
                          UsuarioRepository usuarioRepository) {
        this.reservaRepository = reservaRepository;
        this.escenarioRepository = escenarioRepository;
        this.franjaRepository = franjaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Crea una reserva garantizando atomicidad y prevención de sobre-reserva (Anti-Overbooking).
     * El método es synchronized para asegurar que la transacción de Spring comience y confirme
     * completamente antes de que un hilo concurrente evalúe la disponibilidad del mismo recurso.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public synchronized ReservaResponse crearReserva(Long usuarioId, ReservaRequest request) {
        // Regla HU-08: Validación de fecha no pasada
        LocalDate hoy = LocalDate.now();
        if (request.getFecha().isBefore(hoy)) {
            throw new BusinessException("No se permiten reservas en fechas pasadas (" + request.getFecha() + ").");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));

        Escenario escenario = escenarioRepository.findById(request.getEscenarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Escenario no encontrado con ID: " + request.getEscenarioId()));

        // Regla HU-21: Escenario en mantenimiento
        if (Boolean.TRUE.equals(escenario.getEnMantenimiento())) {
            throw new BusinessException("El escenario '" + escenario.getNombre() + "' está en mantenimiento y no admite reservas.");
        }

        FranjaHoraria franja = franjaRepository.findByIdWithLock(request.getFranjaId())
            .orElseThrow(() -> new ResourceNotFoundException("Franja horaria no encontrada con ID: " + request.getFranjaId()));

        if (!franja.getEscenario().getId().equals(escenario.getId())) {
            throw new BusinessException("La franja horaria seleccionada no corresponde al escenario indicado.");
        }

        if (Boolean.FALSE.equals(franja.getActiva())) {
            throw new BusinessException("La franja horaria seleccionada se encuentra inactiva.");
        }

        // Regla HU-07 y RNF-02: Control de colisión y bloqueo pesimista contra concurrencia
        Optional<Reserva> reservaActiva = reservaRepository.findActiveReservaWithLock(
            escenario.getId(),
            franja.getId(),
            request.getFecha(),
            EstadoReserva.CONFIRMADA
        );

        if (reservaActiva.isPresent()) {
            throw new CollisionException(
                "La franja de " + franja.getHoraInicio() + " a " + franja.getHoraFin() +
                " en el escenario '" + escenario.getNombre() + "' para la fecha " + request.getFecha() +
                " ya se encuentra ocupada por otra reserva."
            );
        }

        // Generar código alfanumérico único para la reserva
        String codigo = "RES-" + request.getFecha().toString().replace("-", "") + "-" +
                        UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Reserva reserva = new Reserva(
            codigo,
            usuario,
            escenario,
            franja,
            request.getFecha(),
            escenario.getTarifaHora()
        );

        Reserva guardada = reservaRepository.save(reserva);
        return mapToResponse(guardada);
    }

    /**
     * Cancela una reserva existente y libera de inmediato la franja horaria (HU-06).
     */
    @Transactional
    public ReservaResponse cancelarReserva(Long reservaId, Long usuarioId, boolean esAdmin, String motivo) {
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con ID: " + reservaId));

        // Regla HU-19: Un usuario regular solo puede cancelar sus propias reservas
        if (!esAdmin && !reserva.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedAccessException("Acceso denegado: No tiene autorización para cancelar reservas de otros usuarios.");
        }

        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new BusinessException("La reserva ya se encuentra en estado cancelado.");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        reserva.setCanceladoEn(LocalDateTime.now());
        reserva.setMotivoCancelacion(motivo != null && !motivo.isBlank() ? motivo : "Cancelada por el usuario");

        Reserva actualizada = reservaRepository.save(reserva);
        return mapToResponse(actualizada);
    }

    /**
     * Consulta la matriz de disponibilidad horaria para un escenario y fecha (WF-02).
     */
    @Transactional(readOnly = true)
    public List<DisponibilidadFranjaResponse> consultarDisponibilidad(Long escenarioId, LocalDate fecha) {
        if (!escenarioRepository.existsById(escenarioId)) {
            throw new ResourceNotFoundException("Escenario no encontrado con ID: " + escenarioId);
        }

        List<FranjaHoraria> franjas = franjaRepository.findByEscenarioIdAndActivaTrueOrderByHoraInicioAsc(escenarioId);
        List<Reserva> reservasActivas = reservaRepository.findByEscenarioIdAndFechaAndEstado(
            escenarioId, fecha, EstadoReserva.CONFIRMADA
        );

        return franjas.stream().map(franja -> {
            Optional<Reserva> match = reservasActivas.stream()
                .filter(r -> r.getFranja().getId().equals(franja.getId()))
                .findFirst();

            if (match.isPresent()) {
                Reserva r = match.get();
                return new DisponibilidadFranjaResponse(
                    franja.getId(),
                    franja.getHoraInicio(),
                    franja.getHoraFin(),
                    false, // Ocupada
                    r.getId(),
                    r.getCodigoReserva()
                );
            } else {
                return new DisponibilidadFranjaResponse(
                    franja.getId(),
                    franja.getHoraInicio(),
                    franja.getHoraFin(),
                    true, // Disponible
                    null,
                    null
                );
            }
        }).collect(Collectors.toList());
    }

    /**
     * Lista las reservas del usuario autenticado (HU-15).
     */
    @Transactional(readOnly = true)
    public List<ReservaResponse> listarMisReservas(Long usuarioId) {
        return reservaRepository.findByUsuarioIdOrderByFechaDesc(usuarioId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Lista todas las reservas (para panel de administración).
     */
    @Transactional(readOnly = true)
    public List<ReservaResponse> listarTodas() {
        return reservaRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReservaResponse obtenerPorId(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con ID: " + reservaId));
        return mapToResponse(reserva);
    }

    private ReservaResponse mapToResponse(Reserva reserva) {
        return new ReservaResponse(
            reserva.getId(),
            reserva.getCodigoReserva(),
            reserva.getUsuario().getId(),
            reserva.getUsuario().getNombre() + " " + reserva.getUsuario().getApellido(),
            reserva.getUsuario().getEmail(),
            reserva.getEscenario().getId(),
            reserva.getEscenario().getNombre(),
            reserva.getEscenario().getDisciplina(),
            reserva.getFranja().getId(),
            reserva.getFranja().getHoraInicio(),
            reserva.getFranja().getHoraFin(),
            reserva.getFecha(),
            reserva.getEstado(),
            reserva.getPrecioTotal(),
            reserva.getCreadoEn(),
            reserva.getCanceladoEn(),
            reserva.getMotivoCancelacion()
        );
    }
}

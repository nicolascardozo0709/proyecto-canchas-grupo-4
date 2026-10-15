package com.canchas.reserva.controller;

import com.canchas.reserva.dto.ReservaDtos.*;
import com.canchas.reserva.entity.Rol;
import com.canchas.reserva.security.CustomUserDetails;
import com.canchas.reserva.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas", description = "Endpoints transaccionales para reserva, cancelación y consulta de disponibilidad")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    @Operation(summary = "Crear una reserva con validación anti-colisión y bloqueo de fechas pasadas (HU-05, HU-07, HU-08)")
    public ResponseEntity<ReservaResponse> crearReserva(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ReservaRequest request
    ) {
        ReservaResponse creada = reservaService.crearReserva(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar una reserva y liberar de inmediato la franja horaria (HU-06, HU-19)")
    public ResponseEntity<ReservaResponse> cancelarReserva(
        @PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody(required = false) CancelacionRequest request
    ) {
        boolean esAdmin = userDetails.getUsuario().getRol() == Rol.ROLE_ADMIN;
        String motivo = request != null ? request.getMotivo() : "Cancelación voluntaria";
        ReservaResponse cancelada = reservaService.cancelarReserva(id, userDetails.getId(), esAdmin, motivo);
        return ResponseEntity.ok(cancelada);
    }

    @GetMapping("/disponibilidad")
    @Operation(summary = "Consultar matriz de disponibilidad horaria por escenario y fecha (WF-02)")
    public ResponseEntity<List<DisponibilidadFranjaResponse>> consultarDisponibilidad(
        @RequestParam Long escenarioId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        List<DisponibilidadFranjaResponse> disponibilidad = reservaService.consultarDisponibilidad(escenarioId, fecha);
        return ResponseEntity.ok(disponibilidad);
    }

    @GetMapping("/mis-reservas")
    @Operation(summary = "Consultar historial de reservas del deportista autenticado (HU-15, HU-19, WF-03)")
    public ResponseEntity<List<ReservaResponse>> listarMisReservas(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<ReservaResponse> misReservas = reservaService.listarMisReservas(userDetails.getId());
        return ResponseEntity.ok(misReservas);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Listar todas las reservas del sistema (Solo Administrador)")
    public ResponseEntity<List<ReservaResponse>> listarTodas() {
        List<ReservaResponse> reservas = reservaService.listarTodas();
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener el detalle de una reserva por su ID")
    public ResponseEntity<ReservaResponse> obtenerPorId(@PathVariable Long id) {
        ReservaResponse reserva = reservaService.obtenerPorId(id);
        return ResponseEntity.ok(reserva);
    }
}

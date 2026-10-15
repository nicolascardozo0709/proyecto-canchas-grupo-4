package com.canchas.reserva.controller;

import com.canchas.reserva.dto.EscenarioDtos.*;
import com.canchas.reserva.entity.Disciplina;
import com.canchas.reserva.service.EscenarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/escenarios")
@Tag(name = "Escenarios Deportivos", description = "Endpoints de catálogo y gestión CRUD de canchas deportivas")
public class EscenarioController {

    private final EscenarioService escenarioService;

    public EscenarioController(EscenarioService escenarioService) {
        this.escenarioService = escenarioService;
    }

    @GetMapping
    @Operation(summary = "Listar escenarios deportivos con filtro opcional por disciplina (WF-01)")
    public ResponseEntity<List<EscenarioResponse>> listarTodos(
        @RequestParam(required = false) Disciplina disciplina
    ) {
        List<EscenarioResponse> escenarios = escenarioService.listarTodos(disciplina);
        return ResponseEntity.ok(escenarios);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener el detalle de un escenario deportivo por su ID")
    public ResponseEntity<EscenarioResponse> obtenerPorId(@PathVariable Long id) {
        EscenarioResponse escenario = escenarioService.obtenerPorId(id);
        return ResponseEntity.ok(escenario);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Registrar un nuevo escenario deportivo (HU-01, Solo Administrador)")
    public ResponseEntity<EscenarioResponse> crear(@Valid @RequestBody EscenarioRequest request) {
        EscenarioResponse creado = escenarioService.crearEscenario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Editar la información de un escenario existente (HU-02, Solo Administrador)")
    public ResponseEntity<EscenarioResponse> actualizar(
        @PathVariable Long id,
        @Valid @RequestBody EscenarioRequest request
    ) {
        EscenarioResponse actualizado = escenarioService.actualizarEscenario(id, request);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Eliminar un escenario deportivo (HU-03, Solo Administrador)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        escenarioService.eliminarEscenario(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/mantenimiento")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Cambiar estado de mantenimiento del escenario (HU-21, Solo Administrador)")
    public ResponseEntity<EscenarioResponse> cambiarMantenimiento(
        @PathVariable Long id,
        @RequestParam boolean enMantenimiento
    ) {
        EscenarioResponse actualizado = escenarioService.cambiarEstadoMantenimiento(id, enMantenimiento);
        return ResponseEntity.ok(actualizado);
    }
}

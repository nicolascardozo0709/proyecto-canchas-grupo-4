package com.canchas.reserva.controller;

import com.canchas.reserva.dto.FranjaDtos.*;
import com.canchas.reserva.service.FranjaHorariaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/franjas")
@Tag(name = "Franjas Horarias", description = "Endpoints para parametrización de bloques de reserva de 60 minutos")
public class FranjaHorariaController {

    private final FranjaHorariaService franjaService;

    public FranjaHorariaController(FranjaHorariaService franjaService) {
        this.franjaService = franjaService;
    }

    @GetMapping("/escenario/{escenarioId}")
    @Operation(summary = "Listar todas las franjas horarias configuradas para un escenario")
    public ResponseEntity<List<FranjaResponse>> listarPorEscenario(@PathVariable Long escenarioId) {
        List<FranjaResponse> franjas = franjaService.listarPorEscenario(escenarioId);
        return ResponseEntity.ok(franjas);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Definir una nueva franja horaria para un escenario (HU-04, Solo Administrador)")
    public ResponseEntity<FranjaResponse> crear(@Valid @RequestBody FranjaRequest request) {
        FranjaResponse creada = franjaService.crearFranja(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Eliminar una franja horaria (Solo Administrador)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        franjaService.eliminarFranja(id);
        return ResponseEntity.noContent().build();
    }
}

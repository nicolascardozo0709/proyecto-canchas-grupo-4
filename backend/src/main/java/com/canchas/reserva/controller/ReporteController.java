package com.canchas.reserva.controller;

import com.canchas.reserva.dto.ReporteDtos.*;
import com.canchas.reserva.dto.ReservaDtos.ReservaResponse;
import com.canchas.reserva.entity.EstadoReserva;
import com.canchas.reserva.exception.BusinessException;
import com.canchas.reserva.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

/**
 * Funcionalidades de datos de la Entrega 3 (solo Administrador):
 * estadísticas de ocupación, gráficos de demanda y reporte filtrable exportable a CSV.
 */
@RestController
@RequestMapping("/api/reportes")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Reportes y Estadísticas", description = "Ocupación por escenario y franja, demanda por día/hora y exportación CSV")
public class ReporteController {

    private static final String BOM_UTF8 = "\uFEFF";

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/estadisticas")
    @Operation(summary = "Estadísticas de ocupación por escenario y franja, y demanda por día de la semana")
    public ResponseEntity<EstadisticasResponse> estadisticas(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
        @RequestParam(required = false) Long escenarioId
    ) {
        return ResponseEntity.ok(reporteService.estadisticas(desde, hasta, escenarioId));
    }

    @GetMapping("/reservas")
    @Operation(summary = "Reporte de reservas filtrable por escenario, usuario, rango de fechas y estado")
    public ResponseEntity<List<ReservaResponse>> reservas(
        @RequestParam(required = false) Long escenarioId,
        @RequestParam(required = false) Long usuarioId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
        @RequestParam(required = false) EstadoReserva estado
    ) {
        return ResponseEntity.ok(reporteService.filtrar(escenarioId, usuarioId, desde, hasta, estado));
    }

    @GetMapping("/reservas/csv")
    @Operation(summary = "Exporta a CSV (UTF-8 con BOM) el mismo reporte filtrable de reservas")
    public ResponseEntity<byte[]> reservasCsv(
        @RequestParam(required = false) Long escenarioId,
        @RequestParam(required = false) Long usuarioId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
        @RequestParam(required = false) EstadoReserva estado,
        @RequestParam(defaultValue = ",") String separador
    ) {
        if (separador.length() != 1) {
            throw new BusinessException("Separador CSV no soportado. Use ',' o ';'.");
        }
        String csv = reporteService.exportarCsv(escenarioId, usuarioId, desde, hasta, estado, separador.charAt(0));
        byte[] cuerpo = (BOM_UTF8 + csv).getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename("reporte_reservas_" + LocalDate.now() + ".csv").build());
        return ResponseEntity.ok().headers(headers).body(cuerpo);
    }

    @GetMapping("/usuarios")
    @Operation(summary = "Usuarios disponibles para el filtro del reporte (id, nombre, correo)")
    public ResponseEntity<List<UsuarioResumen>> usuarios() {
        return ResponseEntity.ok(reporteService.listarUsuarios());
    }
}

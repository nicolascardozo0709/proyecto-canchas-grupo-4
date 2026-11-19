package com.canchas.reserva.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTOs del módulo de datos de la Entrega 3: estadísticas de ocupación,
 * demanda por día/hora y reporte filtrable de reservas.
 */
public class ReporteDtos {

    /** Ocupación de un escenario: reservas vigentes frente a las franjas ofrecidas en el rango. */
    public record OcupacionEscenario(
        Long escenarioId,
        String escenarioNombre,
        String disciplina,
        long reservas,
        long franjasPosibles,
        double ocupacionPct,
        BigDecimal ingresos
    ) {}

    /** Ocupación de una franja horaria (agregada sobre todos los escenarios que la ofrecen). */
    public record OcupacionFranja(
        String horaInicio,
        String horaFin,
        long reservas,
        long franjasPosibles,
        double ocupacionPct
    ) {}

    /** Demanda (número de reservas vigentes) por día de la semana. diaSemana: 1 = lunes ... 7 = domingo. */
    public record DemandaDia(
        int diaSemana,
        String dia,
        long reservas
    ) {}

    public record EstadisticasResponse(
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate desde,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate hasta,
        long diasRango,
        long totalVigentes,
        long totalCanceladas,
        BigDecimal ingresos,
        double ocupacionGlobalPct,
        String diaPico,
        String horaPico,
        List<OcupacionEscenario> porEscenario,
        List<OcupacionFranja> porFranja,
        List<DemandaDia> porDiaSemana
    ) {}

    /** Datos mínimos de usuario para poblar el filtro del reporte (sin contraseña ni datos sensibles). */
    public record UsuarioResumen(Long id, String nombre, String email) {}
}

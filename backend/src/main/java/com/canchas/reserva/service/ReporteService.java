package com.canchas.reserva.service;

import com.canchas.reserva.dto.ReporteDtos.*;
import com.canchas.reserva.dto.ReservaDtos.ReservaResponse;
import com.canchas.reserva.entity.Escenario;
import com.canchas.reserva.entity.EstadoReserva;
import com.canchas.reserva.entity.FranjaHoraria;
import com.canchas.reserva.entity.Reserva;
import com.canchas.reserva.exception.BusinessException;
import com.canchas.reserva.exception.ResourceNotFoundException;
import com.canchas.reserva.repository.EscenarioRepository;
import com.canchas.reserva.repository.FranjaHorariaRepository;
import com.canchas.reserva.repository.ReservaRepository;
import com.canchas.reserva.repository.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Módulo de datos (Entrega 3): estadísticas de ocupación por escenario y por franja,
 * demanda por día/hora y reporte de reservas filtrable y exportable a CSV.
 *
 * <p>Definiciones usadas en todas las métricas:
 * <ul>
 *   <li><b>Reserva vigente</b>: cualquier reserva cuyo estado no es {@code CANCELADA}.</li>
 *   <li><b>Franjas posibles</b>: franjas activas ofrecidas × días del rango analizado.</li>
 *   <li><b>Ocupación (%)</b>: reservas vigentes / franjas posibles × 100.</li>
 * </ul>
 */
@Service
public class ReporteService {

    private static final String[] NOMBRES_DIA = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    /** Rango por defecto cuando no hay filtros ni datos: últimos 30 días. */
    private static final int DIAS_POR_DEFECTO = 30;

    private final ReservaRepository reservaRepository;
    private final EscenarioRepository escenarioRepository;
    private final FranjaHorariaRepository franjaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaService reservaService;

    public ReporteService(ReservaRepository reservaRepository,
                          EscenarioRepository escenarioRepository,
                          FranjaHorariaRepository franjaRepository,
                          UsuarioRepository usuarioRepository,
                          ReservaService reservaService) {
        this.reservaRepository = reservaRepository;
        this.escenarioRepository = escenarioRepository;
        this.franjaRepository = franjaRepository;
        this.usuarioRepository = usuarioRepository;
        this.reservaService = reservaService;
    }

    // -------------------------------------------------------------------------
    // Reporte filtrable
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ReservaResponse> filtrar(Long escenarioId, Long usuarioId,
                                         LocalDate desde, LocalDate hasta, EstadoReserva estado) {
        return buscar(escenarioId, usuarioId, desde, hasta, estado).stream()
            .map(reservaService::mapToResponse)
            .collect(Collectors.toList());
    }

    private List<Reserva> buscar(Long escenarioId, Long usuarioId,
                                 LocalDate desde, LocalDate hasta, EstadoReserva estado) {
        validarRango(desde, hasta);
        Specification<Reserva> spec = (root, query, cb) -> {
            List<Predicate> filtros = new ArrayList<>();
            if (escenarioId != null) filtros.add(cb.equal(root.get("escenario").get("id"), escenarioId));
            if (usuarioId != null) filtros.add(cb.equal(root.get("usuario").get("id"), usuarioId));
            if (desde != null) filtros.add(cb.greaterThanOrEqualTo(root.get("fecha"), desde));
            if (hasta != null) filtros.add(cb.lessThanOrEqualTo(root.get("fecha"), hasta));
            if (estado != null) filtros.add(cb.equal(root.get("estado"), estado));
            query.orderBy(cb.desc(root.get("fecha")), cb.desc(root.get("franja").get("horaInicio")));
            return cb.and(filtros.toArray(new Predicate[0]));
        };
        return reservaRepository.findAll(spec);
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new BusinessException("La fecha inicial no puede ser posterior a la fecha final.");
        }
    }

    @Transactional(readOnly = true)
    public List<UsuarioResumen> listarUsuarios() {
        return usuarioRepository.findAll().stream()
            .map(u -> new UsuarioResumen(u.getId(), (u.getNombre() + " " + (u.getApellido() == null ? "" : u.getApellido())).trim(), u.getEmail()))
            .sorted(Comparator.comparing(UsuarioResumen::nombre, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Exportación CSV
    // -------------------------------------------------------------------------

    /**
     * Genera el CSV del reporte. El separador es configurable porque Excel con configuración regional
     * en español espera ';' mientras que el estándar RFC 4180 usa ','.
     */
    @Transactional(readOnly = true)
    public String exportarCsv(Long escenarioId, Long usuarioId, LocalDate desde, LocalDate hasta,
                              EstadoReserva estado, char separador) {
        if (separador != ',' && separador != ';') {
            throw new BusinessException("Separador CSV no soportado. Use ',' o ';'.");
        }
        List<ReservaResponse> filas = filtrar(escenarioId, usuarioId, desde, hasta, estado);
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(String.valueOf(separador),
            "Código", "Deportista", "Correo", "Escenario", "Disciplina", "Fecha", "Hora inicio",
            "Hora fin", "Estado", "Precio total (COP)", "Creada en", "Cancelada en", "Motivo de cancelación"
        )).append("\r\n");

        for (ReservaResponse r : filas) {
            sb.append(String.join(String.valueOf(separador),
                celdaCsv(r.getCodigoReserva(), separador),
                celdaCsv(r.getUsuarioNombre(), separador),
                celdaCsv(r.getUsuarioEmail(), separador),
                celdaCsv(r.getEscenarioNombre(), separador),
                celdaCsv(r.getDisciplina() != null ? r.getDisciplina().name() : "", separador),
                celdaCsv(String.valueOf(r.getFecha()), separador),
                celdaCsv(r.getHoraInicio() != null ? r.getHoraInicio().format(HORA) : "", separador),
                celdaCsv(r.getHoraFin() != null ? r.getHoraFin().format(HORA) : "", separador),
                celdaCsv(r.getEstado() != null ? r.getEstado().name() : "", separador),
                celdaCsv(r.getPrecioTotal() != null ? r.getPrecioTotal().setScale(0, RoundingMode.HALF_UP).toPlainString() : "", separador),
                celdaCsv(r.getCreadoEn() != null ? r.getCreadoEn().toString().replace('T', ' ') : "", separador),
                celdaCsv(r.getCanceladoEn() != null ? r.getCanceladoEn().toString().replace('T', ' ') : "", separador),
                celdaCsv(r.getMotivoCancelacion(), separador)
            )).append("\r\n");
        }
        return sb.toString();
    }

    /**
     * Escapa una celda CSV. Además de comillas/saltos de línea, neutraliza la "inyección de fórmulas":
     * textos controlados por el usuario (p. ej. el nombre) que empiecen por = + - @ se ejecutarían como
     * fórmula al abrir el archivo en Excel, por lo que se les antepone un apóstrofo.
     */
    static String celdaCsv(String valor, char separador) {
        if (valor == null || valor.isEmpty()) return "";
        String v = valor;
        char primero = v.charAt(0);
        if (primero == '=' || primero == '+' || primero == '-' || primero == '@' || primero == '\t' || primero == '\r') {
            v = "'" + v;
        }
        boolean requiereComillas = v.indexOf(separador) >= 0 || v.indexOf('"') >= 0
            || v.indexOf('\n') >= 0 || v.indexOf('\r') >= 0;
        if (requiereComillas) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    // -------------------------------------------------------------------------
    // Estadísticas
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public EstadisticasResponse estadisticas(LocalDate desde, LocalDate hasta, Long escenarioId) {
        validarRango(desde, hasta);

        List<Escenario> escenarios;
        if (escenarioId != null) {
            Escenario e = escenarioRepository.findById(escenarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Escenario no encontrado con ID: " + escenarioId));
            escenarios = List.of(e);
        } else {
            escenarios = escenarioRepository.findAll();
        }

        // Todas las reservas del alcance (vigentes y canceladas) para el rango pedido.
        List<Reserva> reservas = buscar(escenarioId, null, desde, hasta, null);

        // Rango efectivo: si faltan límites se deducen de los datos (o de los últimos 30 días si no hay datos).
        LocalDate efectivoDesde = desde;
        LocalDate efectivoHasta = hasta;
        if (efectivoDesde == null || efectivoHasta == null) {
            if (reservas.isEmpty()) {
                if (efectivoHasta == null) efectivoHasta = efectivoDesde != null ? efectivoDesde.plusDays(DIAS_POR_DEFECTO - 1L) : LocalDate.now();
                if (efectivoDesde == null) efectivoDesde = efectivoHasta.minusDays(DIAS_POR_DEFECTO - 1L);
            } else {
                LocalDate min = reservas.stream().map(Reserva::getFecha).min(Comparator.naturalOrder()).get();
                LocalDate max = reservas.stream().map(Reserva::getFecha).max(Comparator.naturalOrder()).get();
                if (efectivoDesde == null) efectivoDesde = min;
                if (efectivoHasta == null) efectivoHasta = max;
                if (efectivoDesde.isAfter(efectivoHasta)) efectivoHasta = efectivoDesde;
            }
        }
        long dias = ChronoUnit.DAYS.between(efectivoDesde, efectivoHasta) + 1;

        List<Reserva> vigentes = reservas.stream()
            .filter(r -> r.getEstado() != EstadoReserva.CANCELADA)
            .collect(Collectors.toList());
        long canceladas = reservas.size() - vigentes.size();

        // --- Por escenario ---
        List<OcupacionEscenario> porEscenario = new ArrayList<>();
        Map<LocalTime, long[]> franjaPosibles = new TreeMap<>();      // horaInicio -> [cantidad de franjas activas]
        Map<LocalTime, LocalTime> franjaFin = new HashMap<>();
        long posiblesTotales = 0;

        for (Escenario e : escenarios) {
            List<FranjaHoraria> activas = franjaRepository.findByEscenarioIdAndActivaTrueOrderByHoraInicioAsc(e.getId());
            long posibles = activas.size() * dias;
            posiblesTotales += posibles;

            for (FranjaHoraria f : activas) {
                franjaPosibles.computeIfAbsent(f.getHoraInicio(), k -> new long[1])[0]++;
                franjaFin.putIfAbsent(f.getHoraInicio(), f.getHoraFin());
            }

            List<Reserva> deEsc = vigentes.stream()
                .filter(r -> r.getEscenario().getId().equals(e.getId()))
                .collect(Collectors.toList());
            BigDecimal ingresos = deEsc.stream()
                .map(Reserva::getPrecioTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            porEscenario.add(new OcupacionEscenario(
                e.getId(), e.getNombre(), e.getDisciplina().name(),
                deEsc.size(), posibles, porcentaje(deEsc.size(), posibles), ingresos
            ));
        }
        porEscenario.sort(Comparator.comparingDouble(OcupacionEscenario::ocupacionPct).reversed()
            .thenComparing(OcupacionEscenario::escenarioNombre));

        // --- Por franja horaria ---
        Map<LocalTime, Long> reservasPorHora = vigentes.stream()
            .collect(Collectors.groupingBy(r -> r.getFranja().getHoraInicio(), Collectors.counting()));
        for (Reserva r : vigentes) {   // franjas desactivadas después de reservarse siguen contando como demanda
            franjaFin.putIfAbsent(r.getFranja().getHoraInicio(), r.getFranja().getHoraFin());
            franjaPosibles.computeIfAbsent(r.getFranja().getHoraInicio(), k -> new long[1]);
        }
        List<OcupacionFranja> porFranja = new ArrayList<>();
        for (Map.Entry<LocalTime, long[]> en : franjaPosibles.entrySet()) {
            long posibles = en.getValue()[0] * dias;
            long cant = reservasPorHora.getOrDefault(en.getKey(), 0L);
            LocalTime fin = franjaFin.get(en.getKey());
            porFranja.add(new OcupacionFranja(
                en.getKey().format(HORA), fin != null ? fin.format(HORA) : "", cant, posibles, porcentaje(cant, posibles)
            ));
        }

        // --- Por día de la semana ---
        Map<DayOfWeek, Long> porDia = vigentes.stream()
            .collect(Collectors.groupingBy(r -> r.getFecha().getDayOfWeek(), Collectors.counting()));
        List<DemandaDia> porDiaSemana = new ArrayList<>();
        for (DayOfWeek d : DayOfWeek.values()) {
            porDiaSemana.add(new DemandaDia(d.getValue(), NOMBRES_DIA[d.getValue() - 1], porDia.getOrDefault(d, 0L)));
        }

        String diaPico = porDiaSemana.stream()
            .filter(d -> d.reservas() > 0)
            .max(Comparator.comparingLong(DemandaDia::reservas))
            .map(DemandaDia::dia).orElse(null);
        String horaPico = porFranja.stream()
            .filter(f -> f.reservas() > 0)
            .max(Comparator.comparingLong(OcupacionFranja::reservas))
            .map(f -> f.horaInicio() + " - " + f.horaFin()).orElse(null);

        BigDecimal ingresosTotales = porEscenario.stream()
            .map(OcupacionEscenario::ingresos)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new EstadisticasResponse(
            efectivoDesde, efectivoHasta, dias,
            vigentes.size(), canceladas, ingresosTotales,
            porcentaje(vigentes.size(), posiblesTotales),
            diaPico, horaPico,
            porEscenario, porFranja, porDiaSemana
        );
    }

    /** Porcentaje con un decimal; 0 si no hay capacidad (evita división por cero). */
    static double porcentaje(long parte, long total) {
        if (total <= 0) return 0.0;
        return BigDecimal.valueOf(parte * 100.0 / total).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}

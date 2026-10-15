package com.canchas.reserva.dto;

import com.canchas.reserva.entity.Disciplina;
import com.canchas.reserva.entity.EstadoReserva;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ReservaDtos {

    public static class ReservaRequest {
        @NotNull(message = "El ID del escenario es obligatorio")
        private Long escenarioId;

        @NotNull(message = "El ID de la franja horaria es obligatorio")
        private Long franjaId;

        @NotNull(message = "La fecha de reserva es obligatoria")
        @FutureOrPresent(message = "No se permiten reservas en fechas pasadas")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate fecha;

        public ReservaRequest() {}

        public ReservaRequest(Long escenarioId, Long franjaId, LocalDate fecha) {
            this.escenarioId = escenarioId;
            this.franjaId = franjaId;
            this.fecha = fecha;
        }

        public Long getEscenarioId() { return escenarioId; }
        public void setEscenarioId(Long escenarioId) { this.escenarioId = escenarioId; }
        public Long getFranjaId() { return franjaId; }
        public void setFranjaId(Long franjaId) { this.franjaId = franjaId; }
        public LocalDate getFecha() { return fecha; }
        public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    }

    public static class ReservaResponse {
        private Long id;
        private String codigoReserva;
        private Long usuarioId;
        private String usuarioNombre;
        private String usuarioEmail;
        private Long escenarioId;
        private String escenarioNombre;
        private Disciplina disciplina;
        private Long franjaId;
        @JsonFormat(pattern = "HH:mm")
        private LocalTime horaInicio;
        @JsonFormat(pattern = "HH:mm")
        private LocalTime horaFin;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate fecha;
        private EstadoReserva estado;
        private BigDecimal precioTotal;
        private LocalDateTime creadoEn;
        private LocalDateTime canceladoEn;
        private String motivoCancelacion;

        public ReservaResponse() {}

        public ReservaResponse(Long id, String codigoReserva, Long usuarioId, String usuarioNombre,
                               String usuarioEmail, Long escenarioId, String escenarioNombre,
                               Disciplina disciplina, Long franjaId, LocalTime horaInicio,
                               LocalTime horaFin, LocalDate fecha, EstadoReserva estado,
                               BigDecimal precioTotal, LocalDateTime creadoEn,
                               LocalDateTime canceladoEn, String motivoCancelacion) {
            this.id = id;
            this.codigoReserva = codigoReserva;
            this.usuarioId = usuarioId;
            this.usuarioNombre = usuarioNombre;
            this.usuarioEmail = usuarioEmail;
            this.escenarioId = escenarioId;
            this.escenarioNombre = escenarioNombre;
            this.disciplina = disciplina;
            this.franjaId = franjaId;
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
            this.fecha = fecha;
            this.estado = estado;
            this.precioTotal = precioTotal;
            this.creadoEn = creadoEn;
            this.canceladoEn = canceladoEn;
            this.motivoCancelacion = motivoCancelacion;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getCodigoReserva() { return codigoReserva; }
        public void setCodigoReserva(String codigoReserva) { this.codigoReserva = codigoReserva; }
        public Long getUsuarioId() { return usuarioId; }
        public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
        public String getUsuarioNombre() { return usuarioNombre; }
        public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }
        public String getUsuarioEmail() { return usuarioEmail; }
        public void setUsuarioEmail(String usuarioEmail) { this.usuarioEmail = usuarioEmail; }
        public Long getEscenarioId() { return escenarioId; }
        public void setEscenarioId(Long escenarioId) { this.escenarioId = escenarioId; }
        public String getEscenarioNombre() { return escenarioNombre; }
        public void setEscenarioNombre(String escenarioNombre) { this.escenarioNombre = escenarioNombre; }
        public Disciplina getDisciplina() { return disciplina; }
        public void setDisciplina(Disciplina disciplina) { this.disciplina = disciplina; }
        public Long getFranjaId() { return franjaId; }
        public void setFranjaId(Long franjaId) { this.franjaId = franjaId; }
        public LocalTime getHoraInicio() { return horaInicio; }
        public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
        public LocalTime getHoraFin() { return horaFin; }
        public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
        public LocalDate getFecha() { return fecha; }
        public void setFecha(LocalDate fecha) { this.fecha = fecha; }
        public EstadoReserva getEstado() { return estado; }
        public void setEstado(EstadoReserva estado) { this.estado = estado; }
        public BigDecimal getPrecioTotal() { return precioTotal; }
        public void setPrecioTotal(BigDecimal precioTotal) { this.precioTotal = precioTotal; }
        public LocalDateTime getCreadoEn() { return creadoEn; }
        public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
        public LocalDateTime getCanceladoEn() { return canceladoEn; }
        public void setCanceladoEn(LocalDateTime canceladoEn) { this.canceladoEn = canceladoEn; }
        public String getMotivoCancelacion() { return motivoCancelacion; }
        public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }
    }

    public static class DisponibilidadFranjaResponse {
        private Long franjaId;
        @JsonFormat(pattern = "HH:mm")
        private LocalTime horaInicio;
        @JsonFormat(pattern = "HH:mm")
        private LocalTime horaFin;
        private boolean disponible;
        private Long reservaId;
        private String codigoReserva;

        public DisponibilidadFranjaResponse() {}

        public DisponibilidadFranjaResponse(Long franjaId, LocalTime horaInicio, LocalTime horaFin,
                                            boolean disponible, Long reservaId, String codigoReserva) {
            this.franjaId = franjaId;
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
            this.disponible = disponible;
            this.reservaId = reservaId;
            this.codigoReserva = codigoReserva;
        }

        public Long getFranjaId() { return franjaId; }
        public void setFranjaId(Long franjaId) { this.franjaId = franjaId; }
        public LocalTime getHoraInicio() { return horaInicio; }
        public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
        public LocalTime getHoraFin() { return horaFin; }
        public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
        public boolean isDisponible() { return disponible; }
        public void setDisponible(boolean disponible) { this.disponible = disponible; }
        public Long getReservaId() { return reservaId; }
        public void setReservaId(Long reservaId) { this.reservaId = reservaId; }
        public String getCodigoReserva() { return codigoReserva; }
        public void setCodigoReserva(String codigoReserva) { this.codigoReserva = codigoReserva; }
    }

    public static class CancelacionRequest {
        private String motivo;

        public CancelacionRequest() {}
        public CancelacionRequest(String motivo) { this.motivo = motivo; }

        public String getMotivo() { return motivo; }
        public void setMotivo(String motivo) { this.motivo = motivo; }
    }
}

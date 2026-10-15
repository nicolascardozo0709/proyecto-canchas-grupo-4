package com.canchas.reserva.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public class FranjaDtos {

    public static class FranjaRequest {
        @NotNull(message = "El ID del escenario es obligatorio")
        private Long escenarioId;

        @NotNull(message = "La hora de inicio es obligatoria")
        @JsonFormat(pattern = "HH:mm:ss")
        private LocalTime horaInicio;

        @NotNull(message = "La hora de fin es obligatoria")
        @JsonFormat(pattern = "HH:mm:ss")
        private LocalTime horaFin;

        private Boolean activa = true;

        public FranjaRequest() {}

        public FranjaRequest(Long escenarioId, LocalTime horaInicio, LocalTime horaFin, Boolean activa) {
            this.escenarioId = escenarioId;
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
            this.activa = activa != null ? activa : true;
        }

        public Long getEscenarioId() { return escenarioId; }
        public void setEscenarioId(Long escenarioId) { this.escenarioId = escenarioId; }
        public LocalTime getHoraInicio() { return horaInicio; }
        public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
        public LocalTime getHoraFin() { return horaFin; }
        public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
        public Boolean getActiva() { return activa; }
        public void setActiva(Boolean activa) { this.activa = activa; }
    }

    public static class FranjaResponse {
        private Long id;
        private Long escenarioId;
        private String escenarioNombre;
        @JsonFormat(pattern = "HH:mm")
        private LocalTime horaInicio;
        @JsonFormat(pattern = "HH:mm")
        private LocalTime horaFin;
        private Boolean activa;

        public FranjaResponse() {}

        public FranjaResponse(Long id, Long escenarioId, String escenarioNombre,
                              LocalTime horaInicio, LocalTime horaFin, Boolean activa) {
            this.id = id;
            this.escenarioId = escenarioId;
            this.escenarioNombre = escenarioNombre;
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
            this.activa = activa;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getEscenarioId() { return escenarioId; }
        public void setEscenarioId(Long escenarioId) { this.escenarioId = escenarioId; }
        public String getEscenarioNombre() { return escenarioNombre; }
        public void setEscenarioNombre(String escenarioNombre) { this.escenarioNombre = escenarioNombre; }
        public LocalTime getHoraInicio() { return horaInicio; }
        public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
        public LocalTime getHoraFin() { return horaFin; }
        public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
        public Boolean getActiva() { return activa; }
        public void setActiva(Boolean activa) { this.activa = activa; }
    }
}

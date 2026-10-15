package com.canchas.reserva.dto;

import com.canchas.reserva.entity.Disciplina;
import com.canchas.reserva.entity.Superficie;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class EscenarioDtos {

    public static class EscenarioRequest {
        @NotBlank(message = "El nombre del escenario es obligatorio")
        private String nombre;

        @NotNull(message = "La disciplina deportiva es obligatoria")
        private Disciplina disciplina;

        @NotNull(message = "El tipo de superficie es obligatorio")
        private Superficie superficie;

        @NotNull(message = "El aforo es obligatorio")
        @Min(value = 1, message = "El aforo mínimo es 1 persona")
        private Integer aforo;

        private Boolean iluminacion = true;

        @NotNull(message = "La tarifa por hora es obligatoria")
        @DecimalMin(value = "0.0", inclusive = false, message = "La tarifa debe ser mayor a cero")
        private BigDecimal tarifaHora;

        private Boolean enMantenimiento = false;
        private String descripcion;
        private String imagenUrl;

        public EscenarioRequest() {}

        public EscenarioRequest(String nombre, Disciplina disciplina, Superficie superficie,
                                Integer aforo, Boolean iluminacion, BigDecimal tarifaHora,
                                Boolean enMantenimiento, String descripcion, String imagenUrl) {
            this.nombre = nombre;
            this.disciplina = disciplina;
            this.superficie = superficie;
            this.aforo = aforo;
            this.iluminacion = iluminacion != null ? iluminacion : true;
            this.tarifaHora = tarifaHora;
            this.enMantenimiento = enMantenimiento != null ? enMantenimiento : false;
            this.descripcion = descripcion;
            this.imagenUrl = imagenUrl;
        }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public Disciplina getDisciplina() { return disciplina; }
        public void setDisciplina(Disciplina disciplina) { this.disciplina = disciplina; }
        public Superficie getSuperficie() { return superficie; }
        public void setSuperficie(Superficie superficie) { this.superficie = superficie; }
        public Integer getAforo() { return aforo; }
        public void setAforo(Integer aforo) { this.aforo = aforo; }
        public Boolean getIluminacion() { return iluminacion; }
        public void setIluminacion(Boolean iluminacion) { this.iluminacion = iluminacion; }
        public BigDecimal getTarifaHora() { return tarifaHora; }
        public void setTarifaHora(BigDecimal tarifaHora) { this.tarifaHora = tarifaHora; }
        public Boolean getEnMantenimiento() { return enMantenimiento; }
        public void setEnMantenimiento(Boolean enMantenimiento) { this.enMantenimiento = enMantenimiento; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public String getImagenUrl() { return imagenUrl; }
        public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    }

    public static class EscenarioResponse {
        private Long id;
        private String nombre;
        private Disciplina disciplina;
        private Superficie superficie;
        private Integer aforo;
        private Boolean iluminacion;
        private BigDecimal tarifaHora;
        private Boolean enMantenimiento;
        private String descripcion;
        private String imagenUrl;

        public EscenarioResponse() {}

        public EscenarioResponse(Long id, String nombre, Disciplina disciplina, Superficie superficie,
                                 Integer aforo, Boolean iluminacion, BigDecimal tarifaHora,
                                 Boolean enMantenimiento, String descripcion, String imagenUrl) {
            this.id = id;
            this.nombre = nombre;
            this.disciplina = disciplina;
            this.superficie = superficie;
            this.aforo = aforo;
            this.iluminacion = iluminacion;
            this.tarifaHora = tarifaHora;
            this.enMantenimiento = enMantenimiento;
            this.descripcion = descripcion;
            this.imagenUrl = imagenUrl;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public Disciplina getDisciplina() { return disciplina; }
        public void setDisciplina(Disciplina disciplina) { this.disciplina = disciplina; }
        public Superficie getSuperficie() { return superficie; }
        public void setSuperficie(Superficie superficie) { this.superficie = superficie; }
        public Integer getAforo() { return aforo; }
        public void setAforo(Integer aforo) { this.aforo = aforo; }
        public Boolean getIluminacion() { return iluminacion; }
        public void setIluminacion(Boolean iluminacion) { this.iluminacion = iluminacion; }
        public BigDecimal getTarifaHora() { return tarifaHora; }
        public void setTarifaHora(BigDecimal tarifaHora) { this.tarifaHora = tarifaHora; }
        public Boolean getEnMantenimiento() { return enMantenimiento; }
        public void setEnMantenimiento(Boolean enMantenimiento) { this.enMantenimiento = enMantenimiento; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public String getImagenUrl() { return imagenUrl; }
        public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    }
}

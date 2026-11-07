package com.canchas.reserva.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Data Transfer Objects (DTO) para la interacción con el Asistente Deportivo Inteligente (Gemini AI).
 */
public class AiDtos {

    public static class ChatRequest {
        private String mensaje;
        private List<ChatMessage> historial;

        public ChatRequest() {}

        public ChatRequest(String mensaje, List<ChatMessage> historial) {
            this.mensaje = mensaje;
            this.historial = historial;
        }

        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }

        public List<ChatMessage> getHistorial() { return historial; }
        public void setHistorial(List<ChatMessage> historial) { this.historial = historial; }
    }

    public static class ChatMessage {
        private String remitente; // "USER" o "BOT"
        private String texto;

        public ChatMessage() {}

        public ChatMessage(String remitente, String texto) {
            this.remitente = remitente;
            this.texto = texto;
        }

        public String getRemitente() { return remitente; }
        public void setRemitente(String remitente) { this.remitente = remitente; }

        public String getTexto() { return texto; }
        public void setTexto(String texto) { this.texto = texto; }
    }

    public static class ChatResponse {
        private String respuesta;
        private PropuestaReserva propuestaReserva;
        private String motorUtilizado;

        public ChatResponse() {}

        public ChatResponse(String respuesta, PropuestaReserva propuestaReserva, String motorUtilizado) {
            this.respuesta = respuesta;
            this.propuestaReserva = propuestaReserva;
            this.motorUtilizado = motorUtilizado;
        }

        public String getRespuesta() { return respuesta; }
        public void setRespuesta(String respuesta) { this.respuesta = respuesta; }

        public PropuestaReserva getPropuestaReserva() { return propuestaReserva; }
        public void setPropuestaReserva(PropuestaReserva propuestaReserva) { this.propuestaReserva = propuestaReserva; }

        public String getMotorUtilizado() { return motorUtilizado; }
        public void setMotorUtilizado(String motorUtilizado) { this.motorUtilizado = motorUtilizado; }
    }

    public static class PropuestaReserva {
        private Long escenarioId;
        private String escenarioNombre;
        private String disciplina;
        private Long franjaId;
        private LocalTime horaInicio;
        private LocalTime horaFin;
        private LocalDate fecha;
        private BigDecimal precio;
        private String superficie;

        public PropuestaReserva() {}

        public PropuestaReserva(Long escenarioId, String escenarioNombre, String disciplina,
                                Long franjaId, LocalTime horaInicio, LocalTime horaFin,
                                LocalDate fecha, BigDecimal precio, String superficie) {
            this.escenarioId = escenarioId;
            this.escenarioNombre = escenarioNombre;
            this.disciplina = disciplina;
            this.franjaId = franjaId;
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
            this.fecha = fecha;
            this.precio = precio;
            this.superficie = superficie;
        }

        public Long getEscenarioId() { return escenarioId; }
        public void setEscenarioId(Long escenarioId) { this.escenarioId = escenarioId; }

        public String getEscenarioNombre() { return escenarioNombre; }
        public void setEscenarioNombre(String escenarioNombre) { this.escenarioNombre = escenarioNombre; }

        public String getDisciplina() { return disciplina; }
        public void setDisciplina(String disciplina) { this.disciplina = disciplina; }

        public Long getFranjaId() { return franjaId; }
        public void setFranjaId(Long franjaId) { this.franjaId = franjaId; }

        public LocalTime getHoraInicio() { return horaInicio; }
        public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

        public LocalTime getHoraFin() { return horaFin; }
        public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

        public LocalDate getFecha() { return fecha; }
        public void setFecha(LocalDate fecha) { this.fecha = fecha; }

        public BigDecimal getPrecio() { return precio; }
        public void setPrecio(BigDecimal precio) { this.precio = precio; }

        public String getSuperficie() { return superficie; }
        public void setSuperficie(String superficie) { this.superficie = superficie; }
    }
}

package com.canchas.reserva.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

/**
 * Entidad JPA que representa una franja horaria parametrizada de 60 minutos para un escenario.
 * Cumple con HU-04.
 */
@Entity
@Table(
    name = "franjas_horarias",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_franja_escenario_hora", columnNames = {"escenario_id", "hora_inicio"})
    }
)
public class FranjaHoraria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "escenario_id", nullable = false)
    private Escenario escenario;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    private Boolean activa = true;

    public FranjaHoraria() {}

    public FranjaHoraria(Escenario escenario, LocalTime horaInicio, LocalTime horaFin, Boolean activa) {
        this.escenario = escenario;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.activa = activa != null ? activa : true;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Escenario getEscenario() { return escenario; }
    public void setEscenario(Escenario escenario) { this.escenario = escenario; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
}

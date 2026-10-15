package com.canchas.reserva.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una reserva de un escenario en una fecha y franja horaria.
 * Implementa las reglas de negocio críticas:
 * - HU-05: Creación de reservas.
 * - HU-06: Cancelación de reservas y liberación de franja.
 * - HU-07: Validación anti-overbooking concurrente.
 * - HU-08: Bloqueo de reservas en fechas pasadas.
 */
@Entity
@Table(
    name = "reservas",
    indexes = {
        @Index(name = "idx_reserva_busqueda", columnList = "escenario_id, franja_id, fecha, estado"),
        @Index(name = "idx_reserva_usuario", columnList = "usuario_id, fecha")
    }
)
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_reserva", nullable = false, unique = true, length = 40)
    private String codigoReserva;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "escenario_id", nullable = false)
    private Escenario escenario;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "franja_id", nullable = false)
    private FranjaHoraria franja;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoReserva estado = EstadoReserva.CONFIRMADA;

    @Column(name = "precio_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioTotal;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "cancelado_en")
    private LocalDateTime canceladoEn;

    @Column(name = "motivo_cancelacion", length = 255)
    private String motivoCancelacion;

    @PrePersist
    protected void onCreate() {
        this.creadoEn = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoReserva.CONFIRMADA;
        }
    }

    public Reserva() {}

    public Reserva(String codigoReserva, Usuario usuario, Escenario escenario,
                   FranjaHoraria franja, LocalDate fecha, BigDecimal precioTotal) {
        this.codigoReserva = codigoReserva;
        this.usuario = usuario;
        this.escenario = escenario;
        this.franja = franja;
        this.fecha = fecha;
        this.precioTotal = precioTotal;
        this.estado = EstadoReserva.CONFIRMADA;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoReserva() { return codigoReserva; }
    public void setCodigoReserva(String codigoReserva) { this.codigoReserva = codigoReserva; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Escenario getEscenario() { return escenario; }
    public void setEscenario(Escenario escenario) { this.escenario = escenario; }

    public FranjaHoraria getFranja() { return franja; }
    public void setFranja(FranjaHoraria franja) { this.franja = franja; }

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

package com.canchas.reserva.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa un escenario o cancha deportiva.
 * Cumple con HU-01, HU-02, HU-03 y HU-21.
 */
@Entity
@Table(name = "escenarios")
public class Escenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Disciplina disciplina;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Superficie superficie;

    @Column(nullable = false)
    private Integer aforo = 10;

    @Column(nullable = false)
    private Boolean iluminacion = true;

    @Column(name = "tarifa_hora", nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifaHora = BigDecimal.valueOf(50000.00);

    @Column(name = "en_mantenimiento", nullable = false)
    private Boolean enMantenimiento = false;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    @OneToMany(mappedBy = "escenario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<FranjaHoraria> franjas = new ArrayList<>();

    public Escenario() {}

    public Escenario(String nombre, Disciplina disciplina, Superficie superficie, Integer aforo,
                     Boolean iluminacion, BigDecimal tarifaHora, Boolean enMantenimiento,
                     String descripcion, String imagenUrl) {
        this.nombre = nombre;
        this.disciplina = disciplina;
        this.superficie = superficie;
        this.aforo = aforo;
        this.iluminacion = iluminacion != null ? iluminacion : true;
        this.tarifaHora = tarifaHora != null ? tarifaHora : BigDecimal.valueOf(50000.00);
        this.enMantenimiento = enMantenimiento != null ? enMantenimiento : false;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
    }

    // Getters y Setters
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

    public List<FranjaHoraria> getFranjas() { return franjas; }
    public void setFranjas(List<FranjaHoraria> franjas) { this.franjas = franjas; }
}

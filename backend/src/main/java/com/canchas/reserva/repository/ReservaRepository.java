package com.canchas.reserva.repository;

import com.canchas.reserva.entity.EstadoReserva;
import com.canchas.reserva.entity.Reserva;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    /**
     * Consulta con bloqueo pesimista en base de datos para prevenir colisiones simultáneas (Overbooking).
     * Cumple con HU-07 y RNF-02.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reserva r WHERE r.escenario.id = :escenarioId AND r.franja.id = :franjaId AND r.fecha = :fecha AND r.estado = :estado")
    Optional<Reserva> findActiveReservaWithLock(
        @Param("escenarioId") Long escenarioId,
        @Param("franjaId") Long franjaId,
        @Param("fecha") LocalDate fecha,
        @Param("estado") EstadoReserva estado
    );

    boolean existsByEscenarioIdAndFranjaIdAndFechaAndEstado(
        Long escenarioId, Long franjaId, LocalDate fecha, EstadoReserva estado
    );

    List<Reserva> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    List<Reserva> findByEscenarioIdAndFechaAndEstado(
        Long escenarioId, LocalDate fecha, EstadoReserva estado
    );

    Optional<Reserva> findByCodigoReserva(String codigoReserva);

    long countByEscenarioIdAndFechaAndEstado(Long escenarioId, LocalDate fecha, EstadoReserva estado);
}

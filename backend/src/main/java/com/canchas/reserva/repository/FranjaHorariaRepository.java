package com.canchas.reserva.repository;

import com.canchas.reserva.entity.FranjaHoraria;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FranjaHorariaRepository extends JpaRepository<FranjaHoraria, Long> {
    List<FranjaHoraria> findByEscenarioIdAndActivaTrueOrderByHoraInicioAsc(Long escenarioId);
    List<FranjaHoraria> findByEscenarioIdOrderByHoraInicioAsc(Long escenarioId);
    boolean existsByEscenarioIdAndHoraInicio(Long escenarioId, LocalTime horaInicio);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM FranjaHoraria f WHERE f.id = :id")
    Optional<FranjaHoraria> findByIdWithLock(@Param("id") Long id);
}

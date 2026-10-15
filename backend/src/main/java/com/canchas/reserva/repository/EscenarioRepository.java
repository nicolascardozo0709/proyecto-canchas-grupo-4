package com.canchas.reserva.repository;

import com.canchas.reserva.entity.Disciplina;
import com.canchas.reserva.entity.Escenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscenarioRepository extends JpaRepository<Escenario, Long> {
    List<Escenario> findByDisciplina(Disciplina disciplina);
    List<Escenario> findByEnMantenimientoFalse();
}

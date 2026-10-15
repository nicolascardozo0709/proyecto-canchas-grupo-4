package com.canchas.reserva.service;

import com.canchas.reserva.dto.FranjaDtos.*;
import com.canchas.reserva.entity.Escenario;
import com.canchas.reserva.entity.FranjaHoraria;
import com.canchas.reserva.exception.BusinessException;
import com.canchas.reserva.exception.ResourceNotFoundException;
import com.canchas.reserva.repository.EscenarioRepository;
import com.canchas.reserva.repository.FranjaHorariaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FranjaHorariaService {

    private final FranjaHorariaRepository franjaRepository;
    private final EscenarioRepository escenarioRepository;

    public FranjaHorariaService(FranjaHorariaRepository franjaRepository, EscenarioRepository escenarioRepository) {
        this.franjaRepository = franjaRepository;
        this.escenarioRepository = escenarioRepository;
    }

    @Transactional(readOnly = true)
    public List<FranjaResponse> listarPorEscenario(Long escenarioId) {
        if (!escenarioRepository.existsById(escenarioId)) {
            throw new ResourceNotFoundException("Escenario no encontrado con ID: " + escenarioId);
        }
        return franjaRepository.findByEscenarioIdOrderByHoraInicioAsc(escenarioId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public FranjaResponse crearFranja(FranjaRequest request) {
        Escenario escenario = escenarioRepository.findById(request.getEscenarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Escenario no encontrado con ID: " + request.getEscenarioId()));

        if (!request.getHoraFin().isAfter(request.getHoraInicio())) {
            throw new BusinessException("La hora de fin debe ser posterior a la hora de inicio.");
        }

        if (franjaRepository.existsByEscenarioIdAndHoraInicio(request.getEscenarioId(), request.getHoraInicio())) {
            throw new BusinessException("Ya existe una franja configurada para este escenario iniciando a las " + request.getHoraInicio());
        }

        FranjaHoraria franja = new FranjaHoraria(
            escenario,
            request.getHoraInicio(),
            request.getHoraFin(),
            request.getActiva() != null ? request.getActiva() : true
        );

        FranjaHoraria guardada = franjaRepository.save(franja);
        return mapToResponse(guardada);
    }

    @Transactional
    public void eliminarFranja(Long id) {
        if (!franjaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Franja horaria no encontrada con ID: " + id);
        }
        franjaRepository.deleteById(id);
    }

    private FranjaResponse mapToResponse(FranjaHoraria franja) {
        return new FranjaResponse(
            franja.getId(),
            franja.getEscenario().getId(),
            franja.getEscenario().getNombre(),
            franja.getHoraInicio(),
            franja.getHoraFin(),
            franja.getActiva()
        );
    }
}

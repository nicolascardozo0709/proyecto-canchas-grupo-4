package com.canchas.reserva.service;

import com.canchas.reserva.dto.EscenarioDtos.*;
import com.canchas.reserva.entity.Disciplina;
import com.canchas.reserva.entity.Escenario;
import com.canchas.reserva.exception.ResourceNotFoundException;
import com.canchas.reserva.repository.EscenarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EscenarioService {

    private final EscenarioRepository escenarioRepository;

    public EscenarioService(EscenarioRepository escenarioRepository) {
        this.escenarioRepository = escenarioRepository;
    }

    @Transactional(readOnly = true)
    public List<EscenarioResponse> listarTodos(Disciplina disciplina) {
        List<Escenario> escenarios;
        if (disciplina != null) {
            escenarios = escenarioRepository.findByDisciplina(disciplina);
        } else {
            escenarios = escenarioRepository.findAll();
        }

        return escenarios.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EscenarioResponse obtenerPorId(Long id) {
        Escenario escenario = escenarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Escenario deportivo no encontrado con ID: " + id));
        return mapToResponse(escenario);
    }

    @Transactional
    public EscenarioResponse crearEscenario(EscenarioRequest request) {
        Escenario escenario = new Escenario(
            request.getNombre(),
            request.getDisciplina(),
            request.getSuperficie(),
            request.getAforo(),
            request.getIluminacion(),
            request.getTarifaHora(),
            request.getEnMantenimiento(),
            request.getDescripcion(),
            request.getImagenUrl()
        );

        Escenario guardado = escenarioRepository.save(escenario);
        return mapToResponse(guardado);
    }

    @Transactional
    public EscenarioResponse actualizarEscenario(Long id, EscenarioRequest request) {
        Escenario escenario = escenarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Escenario no encontrado con ID: " + id));

        escenario.setNombre(request.getNombre());
        escenario.setDisciplina(request.getDisciplina());
        escenario.setSuperficie(request.getSuperficie());
        escenario.setAforo(request.getAforo());
        escenario.setIluminacion(request.getIluminacion());
        escenario.setTarifaHora(request.getTarifaHora());
        escenario.setEnMantenimiento(request.getEnMantenimiento());
        escenario.setDescripcion(request.getDescripcion());
        if (request.getImagenUrl() != null && !request.getImagenUrl().isBlank()) {
            escenario.setImagenUrl(request.getImagenUrl());
        }

        Escenario actualizado = escenarioRepository.save(escenario);
        return mapToResponse(actualizado);
    }

    @Transactional
    public void eliminarEscenario(Long id) {
        if (!escenarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar: Escenario no encontrado con ID: " + id);
        }
        escenarioRepository.deleteById(id);
    }

    @Transactional
    public EscenarioResponse cambiarEstadoMantenimiento(Long id, boolean enMantenimiento) {
        Escenario escenario = escenarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Escenario no encontrado con ID: " + id));

        escenario.setEnMantenimiento(enMantenimiento);
        Escenario actualizado = escenarioRepository.save(escenario);
        return mapToResponse(actualizado);
    }

    private EscenarioResponse mapToResponse(Escenario escenario) {
        return new EscenarioResponse(
            escenario.getId(),
            escenario.getNombre(),
            escenario.getDisciplina(),
            escenario.getSuperficie(),
            escenario.getAforo(),
            escenario.getIluminacion(),
            escenario.getTarifaHora(),
            escenario.getEnMantenimiento(),
            escenario.getDescripcion(),
            escenario.getImagenUrl()
        );
    }
}

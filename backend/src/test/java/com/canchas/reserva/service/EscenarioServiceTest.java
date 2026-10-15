package com.canchas.reserva.service;

import com.canchas.reserva.dto.EscenarioDtos.*;
import com.canchas.reserva.entity.Disciplina;
import com.canchas.reserva.entity.Escenario;
import com.canchas.reserva.entity.Superficie;
import com.canchas.reserva.exception.ResourceNotFoundException;
import com.canchas.reserva.repository.EscenarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EscenarioServiceTest {

    @Mock
    private EscenarioRepository escenarioRepository;

    @InjectMocks
    private EscenarioService escenarioService;

    private Escenario escenarioMock;

    @BeforeEach
    void setUp() {
        escenarioMock = new Escenario(
            "Cancha Sintética 1",
            Disciplina.FUTBOL,
            Superficie.SINTETICA,
            16,
            true,
            BigDecimal.valueOf(80000.00),
            false,
            "Cancha para partidos de fútbol 8",
            "http://imagen.jpg"
        );
        escenarioMock.setId(1L);
    }

    @Test
    @DisplayName("HU-01: Debe crear un escenario deportivo correctamente")
    void crearEscenario_Exito() {
        EscenarioRequest request = new EscenarioRequest(
            "Cancha Sintética 1",
            Disciplina.FUTBOL,
            Superficie.SINTETICA,
            16,
            true,
            BigDecimal.valueOf(80000.00),
            false,
            "Cancha para partidos de fútbol 8",
            "http://imagen.jpg"
        );

        when(escenarioRepository.save(any(Escenario.class))).thenReturn(escenarioMock);

        EscenarioResponse response = escenarioService.crearEscenario(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNombre()).isEqualTo("Cancha Sintética 1");
        assertThat(response.getDisciplina()).isEqualTo(Disciplina.FUTBOL);
        verify(escenarioRepository, times(1)).save(any(Escenario.class));
    }

    @Test
    @DisplayName("HU-02: Debe actualizar un escenario deportivo existente")
    void actualizarEscenario_Exito() {
        EscenarioRequest updateRequest = new EscenarioRequest(
            "Cancha Sintética Renovada",
            Disciplina.FUTBOL,
            Superficie.SINTETICA,
            18,
            true,
            BigDecimal.valueOf(90000.00),
            false,
            "Grama sintética recién cambiada",
            "http://imagen2.jpg"
        );

        when(escenarioRepository.findById(1L)).thenReturn(Optional.of(escenarioMock));
        when(escenarioRepository.save(any(Escenario.class))).thenReturn(escenarioMock);

        EscenarioResponse response = escenarioService.actualizarEscenario(1L, updateRequest);

        assertThat(response).isNotNull();
        assertThat(escenarioMock.getNombre()).isEqualTo("Cancha Sintética Renovada");
        assertThat(escenarioMock.getAforo()).isEqualTo(18);
        verify(escenarioRepository).save(escenarioMock);
    }

    @Test
    @DisplayName("HU-03: Debe eliminar un escenario existente")
    void eliminarEscenario_Exito() {
        when(escenarioRepository.existsById(1L)).thenReturn(true);
        doNothing().when(escenarioRepository).deleteById(1L);

        escenarioService.eliminarEscenario(1L);

        verify(escenarioRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("HU-03: Debe lanzar ResourceNotFoundException al intentar eliminar un escenario inexistente")
    void eliminarEscenario_NoExiste_LanzaExcepcion() {
        when(escenarioRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> escenarioService.eliminarEscenario(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");

        verify(escenarioRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("HU-21: Debe marcar un escenario en mantenimiento")
    void cambiarEstadoMantenimiento_Exito() {
        when(escenarioRepository.findById(1L)).thenReturn(Optional.of(escenarioMock));
        when(escenarioRepository.save(any(Escenario.class))).thenReturn(escenarioMock);

        EscenarioResponse response = escenarioService.cambiarEstadoMantenimiento(1L, true);

        assertThat(response).isNotNull();
        assertThat(escenarioMock.getEnMantenimiento()).isTrue();
        verify(escenarioRepository).save(escenarioMock);
    }

    @Test
    @DisplayName("Debe listar escenarios filtrados por disciplina deportiva")
    void listarTodos_ConFiltroDisciplina() {
        when(escenarioRepository.findByDisciplina(Disciplina.FUTBOL)).thenReturn(List.of(escenarioMock));

        List<EscenarioResponse> lista = escenarioService.listarTodos(Disciplina.FUTBOL);

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).getDisciplina()).isEqualTo(Disciplina.FUTBOL);
        verify(escenarioRepository).findByDisciplina(Disciplina.FUTBOL);
        verify(escenarioRepository, never()).findAll();
    }
}

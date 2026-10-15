package com.canchas.reserva.integration;

import com.canchas.reserva.dto.ReservaDtos.ReservaRequest;
import com.canchas.reserva.entity.EstadoReserva;
import com.canchas.reserva.exception.CollisionException;
import com.canchas.reserva.repository.ReservaRepository;
import com.canchas.reserva.service.ReservaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AntiColisionConcurrencyTest {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ReservaRepository reservaRepository;

    @Test
    @DisplayName("RNF-02 & HU-07: Concurrencia Extrema - 8 hilos simultáneos para la misma franja y fecha")
    void testConcurrenciaAntiOverbooking() throws InterruptedException {
        int numHilos = 8;
        ExecutorService executor = Executors.newFixedThreadPool(numHilos);
        CountDownLatch latchInicio = new CountDownLatch(1);
        CountDownLatch latchFin = new CountDownLatch(numHilos);

        LocalDate fechaPrueba = LocalDate.now().plusDays(10);
        Long escenarioId = 1L;
        Long franjaId = 3L;

        AtomicInteger reservasExitosas = new AtomicInteger(0);
        AtomicInteger colisionesDetectadas = new AtomicInteger(0);

        for (int i = 0; i < numHilos; i++) {
            executor.submit(() -> {
                try {
                    latchInicio.await(); // Todos los hilos esperan la señal de inicio sincronizada
                    ReservaRequest req = new ReservaRequest(escenarioId, franjaId, fechaPrueba);
                    reservaService.crearReserva(2L, req);
                    reservasExitosas.incrementAndGet();
                } catch (CollisionException e) {
                    colisionesDetectadas.incrementAndGet();
                } catch (Exception e) {
                    // Si ocurre otra excepción transaccional
                } finally {
                    latchFin.countDown();
                }
            });
        }

        // Disparar todos los hilos en el mismo instante exacto
        latchInicio.countDown();
        latchFin.await();
        executor.shutdown();

        // Verificaciones críticas de integridad transaccional
        assertThat(reservasExitosas.get()).isEqualTo(1);
        assertThat(colisionesDetectadas.get()).isEqualTo(numHilos - 1);

        long reservasEnBd = reservaRepository.countByEscenarioIdAndFechaAndEstado(
            escenarioId, fechaPrueba, EstadoReserva.CONFIRMADA
        );
        assertThat(reservasEnBd).isEqualTo(1);
    }
}

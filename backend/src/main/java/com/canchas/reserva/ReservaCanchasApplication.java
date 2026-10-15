package com.canchas.reserva;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Clase principal de inicio para la Plataforma de Reserva de Escenarios Deportivos.
 * Proyecto 3 - Desarrollo de Software 2026-3 (Pontificia Universidad Javeriana)
 * 
 * @author Nicolás Cardozo Mejía (Líder Frontend y Arquitectura)
 * @author Samuel Correa (Líder de Requisitos y DevOps)
 * @author Nicolás Espinosa Soler (Líder Backend y DBA)
 * @author Gustavo Mejía (Líder QA y Product Owner)
 */
@SpringBootApplication
@EnableTransactionManagement
public class ReservaCanchasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservaCanchasApplication.class, args);
    }
}

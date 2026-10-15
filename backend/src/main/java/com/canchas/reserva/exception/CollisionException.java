package com.canchas.reserva.exception;

/**
 * Excepción lanzada cuando ocurre un intento de sobre-reserva (overbooking) o colisión concurrente.
 * Mapea a HTTP 409 Conflict. Cumple con HU-07.
 */
public class CollisionException extends RuntimeException {
    public CollisionException(String message) {
        super(message);
    }
}

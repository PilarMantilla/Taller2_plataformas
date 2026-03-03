package com.universidad.reservas.model;

import java.time.LocalDate;

public record Reserva(
                String codigoReserva,
                String huesped,
                Habitacion habitacion,
                LocalDate fechaEntrada,
                LocalDate fechaSalida,
                double totalPagado) {
}

package com.universidad.reservas.service;

import com.universidad.reservas.model.Habitacion;
import com.universidad.reservas.model.Reserva;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class ServicioReservas {

    private static final double DESCUENTO_LARGA_ESTADIA = 0.10;

    private final PasarelaPago pasarelaPago;
    private final ServicioNotificacion servicioNotificacion;

    public ServicioReservas(PasarelaPago pasarelaPago, ServicioNotificacion servicioNotificacion) {
        this.pasarelaPago = pasarelaPago;
        this.servicioNotificacion = servicioNotificacion;
    }

    public Reserva crearReserva(String huesped, Habitacion habitacion,
            LocalDate fechaEntrada, LocalDate fechaSalida) {

        if (!habitacion.disponible()) {
            throw new IllegalStateException(
                    "La habitación %s no está disponible".formatted(habitacion.id()));
        }

        if (!fechaSalida.isAfter(fechaEntrada)) {
            throw new IllegalArgumentException(
                    "La fecha de salida debe ser posterior a la fecha de entrada");
        }

        long noches = ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
        double total = calcularTotal(habitacion.precioPorNoche(), noches);

        boolean pagoExitoso = pasarelaPago.procesarCobro(huesped, total);

        if (!pagoExitoso) {
            throw new IllegalStateException(
                    "El pago de $%.2f para %s fue rechazado".formatted(total, huesped));
        }

        var reserva = new Reserva(
                UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                huesped,
                habitacion,
                fechaEntrada,
                fechaSalida,
                total);

        servicioNotificacion.enviarConfirmacion(reserva);

        return reserva;
    }

    double calcularTotal(double precioPorNoche, long noches) {
        double total = precioPorNoche * noches;
        if (noches >= 7) {
            total -= total * DESCUENTO_LARGA_ESTADIA;
        }
        return total;
    }
}

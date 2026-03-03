package com.universidad.reservas.service;

import com.universidad.reservas.model.Reserva;

public interface ServicioNotificacion {

    void enviarConfirmacion(Reserva reserva);
}

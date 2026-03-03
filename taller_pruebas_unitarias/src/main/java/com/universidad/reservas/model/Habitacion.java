package com.universidad.reservas.model;

public record Habitacion(
        String id,
        TipoHabitacion tipo,
        double precioPorNoche,
        boolean disponible) {

    public Habitacion {
        if (precioPorNoche < 0) {
            throw new IllegalArgumentException("El precio por noche no puede ser negativo");
        }
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id de la habitación no puede estar vacío");
        }
    }
}

package com.universidad.reservas.service;

import com.universidad.reservas.model.Habitacion;
import com.universidad.reservas.model.Reserva; // Se usará en Bloque 3
import com.universidad.reservas.model.TipoHabitacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║ TALLER: Pruebas Unitarias con JUnit 5 y Mockito ║
 * ║ Sistema de Reservas de Hotel ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║ INSTRUCCIONES: ║
 * ║ 1. Lee cada bloque @Test y su // TODO con atención. ║
 * ║ 2. Implementa el cuerpo de cada prueba siguiendo el patrón ║
 * ║ AAA (Arrange - Act - Assert). ║
 * ║ 3. Ejecuta las pruebas con: ./gradlew test ║
 * ║ 4. Todas las pruebas deben pasar en VERDE al terminar. ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ServicioReservas - Taller de Pruebas Unitarias")
class ServicioReservasTest {

    // ─── Dependencias mockeadas ───────────────────────────────────────
    @Mock
    private PasarelaPago pasarelaPago;

    @Mock
    private ServicioNotificacion servicioNotificacion;

    // ─── System Under Test (SUT) ──────────────────────────────────────
    private ServicioReservas servicioReservas;

    // ─── Datos de prueba reutilizables ────────────────────────────────
    private Habitacion habitacionDisponible;
    private Habitacion habitacionNoDisponible;
    private LocalDate hoy;
    private LocalDate manana;

    @BeforeEach
    void setUp() {
        // Inicializa el SUT con los mocks
        servicioReservas = new ServicioReservas(pasarelaPago, servicioNotificacion);

        // datos de prueba reutilizables
        habitacionDisponible = new Habitacion("H-101", TipoHabitacion.DOBLE, 120.0, true);
        habitacionNoDisponible = new Habitacion("H-202", TipoHabitacion.SUITE, 250.0, false);
        hoy = LocalDate.now();
        manana = hoy.plusDays(1);
    }

    // ═══════════════════════════════════════════════════════════════════
    // BLOQUE 1: Camino feliz (Happy Path)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Camino feliz - Reservas exitosas")
    class CaminoFeliz {

        @Test
        @DisplayName("Debe crear una reserva exitosa para una estancia de 3 noches")
        void debeCrearReservaExitosaParaTresNoches() {
            // ARRANGE
            when(pasarelaPago.procesarCobro(eq("Carlos Pérez"), eq(360.0)))
                    .thenReturn(true);

            // ACT
            var reserva = servicioReservas.crearReserva(
                    "Carlos Pérez",
                    habitacionDisponible,
                    hoy,
                    hoy.plusDays(3));

            // ASSERT
            assertNotNull(reserva);
            assertEquals("Carlos Pérez", reserva.huesped());
            assertEquals(360.0, reserva.totalPagado());
            assertNotNull(reserva.codigoReserva());
        }

        @Test
        @DisplayName("Debe aplicar 10% de descuento para estancias de 7+ noches")
        void debeAplicarDescuentoParaEstanciaLarga() {
            // ARRANGE
            when(pasarelaPago.procesarCobro(eq("Ana López"), eq(756.0)))
                    .thenReturn(true);

            // ACT
            var reserva = servicioReservas.crearReserva(
                    "Ana López",
                    habitacionDisponible,
                    hoy,
                    hoy.plusDays(7));

            // ASSERT
            assertNotNull(reserva);
            assertEquals(756.0, reserva.totalPagado());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // BLOQUE 2: Manejo de errores (Excepciones)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Manejo de errores - Excepciones esperadas")
    class ManejoErrores {

        @Test
        @DisplayName("Debe lanzar IllegalStateException si la habitación NO está disponible")
        void debeLanzarExcepcionSiHabitacionNoDisponible() {
            // ACT & ASSERT
            var ex = assertThrows(IllegalStateException.class, () ->
                    servicioReservas.crearReserva(
                            "Invitado",
                            habitacionNoDisponible,
                            hoy,
                            manana));

            assertTrue(ex.getMessage().contains("no está disponible"));
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException si la fecha de salida no es posterior a la de entrada")
        void debeLanzarExcepcionSiFechasSonInvalidas() {
            var ex = assertThrows(IllegalArgumentException.class, () ->
                    servicioReservas.crearReserva(
                            "Invitado",
                            habitacionDisponible,
                            hoy,
                            hoy));

            assertTrue(ex.getMessage().contains("fecha de salida"));
        }

        @Test
        @DisplayName("Debe lanzar IllegalStateException cuando el pago es rechazado")
        void debeLanzarExcepcionSiPagoRechazado() {
            // ARRANGE
            when(pasarelaPago.procesarCobro(anyString(), anyDouble()))
                    .thenReturn(false);

            // ACT & ASSERT
            var ex = assertThrows(IllegalStateException.class, () ->
                    servicioReservas.crearReserva(
                            "Invitado",
                            habitacionDisponible,
                            hoy,
                            manana));

            assertTrue(ex.getMessage().contains("rechazado"));
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // BLOQUE 3: Verificación de interacciones (verify)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Verificación de interacciones con mocks")
    class VerificacionInteracciones {

        @Test
        @DisplayName("Debe enviar notificación exactamente UNA vez después de una reserva exitosa")
        void debeEnviarNotificacionUnaVez() {
            // ARRANGE
            when(pasarelaPago.procesarCobro(anyString(), anyDouble()))
                    .thenReturn(true);

            // ACT
            servicioReservas.crearReserva(
                    "Usuario",
                    habitacionDisponible,
                    hoy,
                    manana);

            // ASSERT
            verify(servicioNotificacion, times(1))
                    .enviarConfirmacion(any(Reserva.class));
        }

        @Test
        @DisplayName("NO debe enviar notificación si el pago fue rechazado")
        void noDebeEnviarNotificacionSiPagoFalla() {
            // ARRANGE
            when(pasarelaPago.procesarCobro(anyString(), anyDouble()))
                    .thenReturn(false);

            // ACT & ASSERT
            assertThrows(IllegalStateException.class, () ->
                    servicioReservas.crearReserva(
                            "Usuario",
                            habitacionDisponible,
                            hoy,
                            manana));

            // ASSERT
            verify(servicioNotificacion, never()).enviarConfirmacion(any());
        }

        @Test
        @DisplayName("Debe llamar a procesarCobro con el monto correcto (sin descuento)")
        void debeCobraMontoCorrectoSinDescuento() {
            // ARRANGE
            when(pasarelaPago.procesarCobro(anyString(), anyDouble()))
                    .thenReturn(true);

            // ACT
            servicioReservas.crearReserva(
                    "Luis García",
                    habitacionDisponible,
                    hoy,
                    hoy.plusDays(2));

            // ASSERT
            verify(pasarelaPago).procesarCobro("Luis García", 240.0);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // BLOQUE 4: Prueba del cálculo interno (BONUS)
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Bonus - Cálculo del total")
    class CalculoTotal {

        @Test
        @DisplayName("calcularTotal: sin descuento para menos de 7 noches")
        void calcularTotalSinDescuento() {
            // ACT
            double total = servicioReservas.calcularTotal(100.0, 5);

            // ASSERT
            assertEquals(500.0, total);
        }

        @Test
        @DisplayName("calcularTotal: con descuento del 10% para 7+ noches")
        void calcularTotalConDescuento() {
            // ACT
            double total = servicioReservas.calcularTotal(200.0, 10);

            // ASSERT
            assertEquals(1800.0, total);
        }
    }
}

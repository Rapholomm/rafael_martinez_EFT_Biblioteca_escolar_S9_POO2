package servicio;

import interfaces.OperacionListener;
import modelo.Prestamo;

import java.sql.SQLException;


public class HiloPrestamo extends Thread {

    // ventana sigue respondiendo

    private static final int RETARDO_DEMO_MS = 1500;

    private static int contador = 0;

    private final int idEstudiante;
    private final int idLibro;
    private final OperacionListener<Prestamo> listener;

    public HiloPrestamo(int idEstudiante, int idLibro, OperacionListener<Prestamo> listener) {
        super("hilo-prestamo-" + (++contador));
        this.idEstudiante = idEstudiante;
        this.idLibro = idLibro;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(RETARDO_DEMO_MS);
            // aqui entra el metodo synchronized: si hay otros hilos prestando, esperan su turno
            Prestamo prestamo = ServicioPrestamos.getInstance().registrarPrestamo(idEstudiante, idLibro);
            listener.alExito(prestamo);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            listener.alFallar("La operación fue interrumpida.");
        } catch (PrestamoException ex) {
            listener.alFallar(ex.getMessage());
        } catch (SQLException ex) {
            listener.alFallar("Error de base de datos: " + ex.getMessage());
        }
    }
}
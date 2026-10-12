package servicio;

import modelo.Prestamo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;

// deja la devoluciones en (registro_atrasos.log)
public final class RegistroAtrasos {

    public static final String ARCHIVO = "registro_atrasos.log";

    private RegistroAtrasos() {
    }

    // synchronized: si dos devoluciones atrasadas ocurren a la vez, las lineas no se mezclan
    public static synchronized void registrar(Prestamo prestamo, int diasAtraso) {
        String linea = LocalDate.now() + " | Préstamo #" + prestamo.getId()
                + " | Estudiante: " + prestamo.getNombreEstudiante()
                + " | Libro: " + prestamo.getTituloLibro()
                + " | Vencía: " + prestamo.getFechaVencimiento()
                + " | Atraso: " + diasAtraso + " día(s)" + System.lineSeparator();
        try {
            Files.writeString(Path.of(ARCHIVO), linea, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            System.err.println("No se pudo escribir " + ARCHIVO + ": " + ex.getMessage());
        }
    }
}

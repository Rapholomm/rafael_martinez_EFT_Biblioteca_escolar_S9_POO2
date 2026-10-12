package vista;

import javax.swing.*;
import java.awt.Component;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

// mensajes de la interfaz
public final class Dialogos {

    private static final String TITULO = "Biblioteca Escolar";

    private Dialogos() {
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, TITULO, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void datoInvalido(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Datos inválidos", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean confirmar(Component padre, String mensaje) {
        return JOptionPane.showConfirmDialog(padre, mensaje, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static void errorSQL(Component padre, String accion, SQLException ex) {
        String detalle;
        String mensaje = ex.getMessage() == null ? "" : ex.getMessage();
        if (ex instanceof SQLIntegrityConstraintViolationException && mensaje.contains("Duplicate")) {
            detalle = "Ya existe un registro con el mismo valor único (RUT, correo, ISBN o nombre).";
        } else if (ex instanceof SQLIntegrityConstraintViolationException) {
            detalle = "El registro está relacionado con otros datos (por ejemplo, préstamos o libros).\n"
                    + "Elimina o modifica primero los registros asociados.";
        } else if (ex.getSQLState() != null && ex.getSQLState().startsWith("08")) {
            detalle = "No hay conexión con la base de datos.\n"
                    + "Revisa que MySQL esté encendido y los datos de db.properties.";
        } else {
            detalle = mensaje;
        }
        JOptionPane.showMessageDialog(padre, "No se pudo " + accion + ":\n" + detalle,
                "Error de base de datos", JOptionPane.ERROR_MESSAGE);
    }
}
package main;

import dao.DatabaseConnection;
import vista.LoginFrame;

import javax.swing.*;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {

        // al cerrar la aplicacion se cierra la coneccion
        Runtime.getRuntime().addShutdownHook(new Thread(() -> DatabaseConnection.getInstance().cerrar()));

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignorada) {

            }


            try {
                DatabaseConnection.getInstance().probarConexion();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null,
                        "No se pudo conectar a la base de datos biblioteca:\n" + ex.getMessage()
                                + "\n\nRevisa que MySQL esté encendido, que hayas ejecutado los scripts de /sql\n"
                                + "y los datos de db.properties (copia db.properties.example).",
                        "Error de conexión", JOptionPane.WARNING_MESSAGE);
            }
            new LoginFrame().setVisible(true);
        });
    }
}
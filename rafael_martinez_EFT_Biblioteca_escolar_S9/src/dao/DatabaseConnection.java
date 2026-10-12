package dao;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/*
 * Patron Singleton: existe una sola instancia de DatabaseConnection y esa instancia

 */
public class DatabaseConnection {

    private static final String ARCHIVO_CONFIG = "db.properties";
    private static final String URL_POR_DEFECTO = "jdbc:mysql://localhost:3306/biblioteca";

    private static DatabaseConnection instancia;

    private final String url;
    private final String usuario;
    private final String password;
    private Connection conexion;

    // constructor privado: nadie mas puede hacer "new DatabaseConnection()"
    private DatabaseConnection() {
        Properties config = new Properties();
        File archivo = new File(ARCHIVO_CONFIG);
        if (archivo.exists()) {
            try (Reader lector = new InputStreamReader(Files.newInputStream(archivo.toPath()), StandardCharsets.UTF_8)) {
                config.load(lector);
            } catch (IOException ex) {
                System.err.println("No se pudo leer " + ARCHIVO_CONFIG + ", se usan los valores por defecto: " + ex.getMessage());
            }
        }
        this.url = config.getProperty("db.url", URL_POR_DEFECTO);
        this.usuario = config.getProperty("db.user", "root");
        this.password = config.getProperty("db.password", "");
    }

    // synchronized para que dos hilos que lo pidan a la vez no creen dos instancias
    public static synchronized DatabaseConnection getInstance() {
        if (instancia == null) {
            instancia = new DatabaseConnection();
        }
        return instancia;
    }

    /**
     * Devuelve la conexion unica. Si no existe, se cerro o se cayo (por ejemplo MySQL se reinicio),
     * se vuelve a abrir con DriverManager.getConnection. Los DAO NO deben cerrarla.
     */
    public synchronized Connection getConnection() throws SQLException {
        if (conexion == null || conexion.isClosed() || !conexion.isValid(2)) {
            cerrarSilenciosamente();
            conexion = DriverManager.getConnection(url, usuario, password);
        }
        return conexion;
    }

    // lanza SQLException si no se puede conectar (la usa Main antes de abrir la interfaz)
    public void probarConexion() throws SQLException {
        getConnection();
    }

    public synchronized void cerrar() {
        cerrarSilenciosamente();
    }

    private void cerrarSilenciosamente() {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException ignorada) {
                // si ya estaba rota no hay nada mas que hacer
            }
            conexion = null;
        }
    }
}

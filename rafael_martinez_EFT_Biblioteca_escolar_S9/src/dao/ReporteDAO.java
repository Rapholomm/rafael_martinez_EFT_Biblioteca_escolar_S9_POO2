package dao;

import modelo.Prestamo;
import modelo.ResultadoReporte;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// consultas de solo lectura para los reportes
public class ReporteDAO {

    public ResultadoReporte librosMasPrestados(int limite) throws SQLException {
        String sql = "SELECT l.titulo, l.autor, COUNT(p.id) AS total "
                + "FROM libros l JOIN prestamos p ON p.id_libro = l.id "
                + "GROUP BY l.id, l.titulo, l.autor "
                + "ORDER BY total DESC, l.titulo LIMIT ?";
        List<Object[]> filas = new ArrayList<>();
        Connection conexion = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setInt(1, limite);
            try (ResultSet rs = statement.executeQuery()) {
                int posicion = 1;
                while (rs.next()) {
                    filas.add(new Object[]{posicion++, rs.getString("titulo"), rs.getString("autor"), rs.getInt("total")});
                }
            }
        }
        return new ResultadoReporte("Libros más prestados",
                new String[]{"N°", "Título", "Autor", "Veces prestado"}, filas);
    }

    public ResultadoReporte historialPorEstudiante(int idEstudiante) throws SQLException {
        String sql = "SELECT l.titulo, l.autor, p.fecha_prestamo, p.fecha_devolucion, p.devuelto "
                + "FROM prestamos p JOIN libros l ON l.id = p.id_libro "
                + "WHERE p.id_estudiante = ? ORDER BY p.fecha_prestamo DESC, p.id DESC";
        List<Object[]> filas = new ArrayList<>();
        Connection conexion = DatabaseConnection.getInstance().getConnection();
        LocalDate hoy = LocalDate.now();

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setInt(1, idEstudiante);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    LocalDate vence = aFecha(rs.getDate("fecha_devolucion"));
                    filas.add(new Object[]{rs.getString("titulo"), rs.getString("autor"),
                            aFecha(rs.getDate("fecha_prestamo")), vence,
                            Prestamo.textoEstado(rs.getBoolean("devuelto"), vence, hoy)});
                }
            }
        }
        return new ResultadoReporte("Historial del estudiante",
                new String[]{"Título", "Autor", "Fecha préstamo", "Vencimiento", "Estado"}, filas);
    }

    public ResultadoReporte librosEnPrestamo() throws SQLException {
        String sql = "SELECT l.titulo, e.nombre AS estudiante, e.curso, p.fecha_prestamo, p.fecha_devolucion "
                + "FROM prestamos p "
                + "JOIN libros l ON l.id = p.id_libro "
                + "JOIN estudiantes e ON e.id = p.id_estudiante "
                + "WHERE p.devuelto = FALSE ORDER BY p.fecha_devolucion, p.id";
        List<Object[]> filas = new ArrayList<>();
        Connection conexion = DatabaseConnection.getInstance().getConnection();
        LocalDate hoy = LocalDate.now();

        try (PreparedStatement statement = conexion.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                LocalDate vence = aFecha(rs.getDate("fecha_devolucion"));
                filas.add(new Object[]{rs.getString("titulo"), rs.getString("estudiante"), rs.getString("curso"),
                        aFecha(rs.getDate("fecha_prestamo")), vence, Prestamo.textoEstado(false, vence, hoy)});
            }
        }
        return new ResultadoReporte("Libros actualmente en préstamo",
                new String[]{"Título", "Estudiante", "Curso", "Fecha préstamo", "Vencimiento", "Estado"}, filas);
    }

    private LocalDate aFecha(Date fecha) {
        return fecha == null ? null : fecha.toLocalDate();
    }
}


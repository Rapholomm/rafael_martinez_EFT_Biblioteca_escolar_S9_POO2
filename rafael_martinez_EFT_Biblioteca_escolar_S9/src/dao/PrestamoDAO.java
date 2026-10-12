package dao;

import modelo.Prestamo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAO implements CrudDAO<Prestamo> {

    private static final String SELECT_BASE =
            "SELECT p.id, p.id_estudiante, p.id_libro, p.fecha_prestamo, p.fecha_devolucion, p.devuelto, "
                    + "e.nombre AS estudiante, l.titulo AS libro "
                    + "FROM prestamos p "
                    + "JOIN estudiantes e ON e.id = p.id_estudiante "
                    + "JOIN libros l ON l.id = p.id_libro";

    @Override
    public void create(Prestamo prestamo) throws SQLException {
        create(DatabaseConnection.getInstance().getConnection(), prestamo);
    }

    // version que usa la conexion de una transaccion (la llama ServicioPrestamos)
    public void create(Connection conexion, Prestamo prestamo) throws SQLException {
        String sql = "INSERT INTO prestamos (id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, prestamo.getIdEstudiante());
            statement.setInt(2, prestamo.getIdLibro());
            statement.setDate(3, Date.valueOf(prestamo.getFechaPrestamo()));
            statement.setDate(4, Date.valueOf(prestamo.getFechaVencimiento()));
            statement.setBoolean(5, prestamo.isDevuelto());
            statement.executeUpdate();
            try (ResultSet claves = statement.getGeneratedKeys()) {
                if (claves.next()) {
                    prestamo.setId(claves.getInt(1));
                }
            }
        }
    }

    @Override
    public List<Prestamo> readAll() throws SQLException {
        return consultar(SELECT_BASE + " ORDER BY p.id DESC");
    }

    public List<Prestamo> readByEstudiante(int idEstudiante) throws SQLException {
        return consultar(SELECT_BASE + " WHERE p.id_estudiante = ? ORDER BY p.id DESC", idEstudiante);
    }

    @Override
    public Prestamo readById(int id) throws SQLException {
        List<Prestamo> resultado = consultar(SELECT_BASE + " WHERE p.id = ?", id);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    // prestamos sin devolver de un estudiante (para el limite de prestamos activos)
    public int contarActivos(int idEstudiante) throws SQLException {
        String sql = "SELECT COUNT(*) FROM prestamos WHERE id_estudiante = ? AND devuelto = FALSE";
        return contar(sql, idEstudiante, null);
    }

    // true si el estudiante ya tiene ese mismo libro sin devolver
    public boolean tieneActivo(int idEstudiante, int idLibro) throws SQLException {
        String sql = "SELECT COUNT(*) FROM prestamos WHERE id_estudiante = ? AND id_libro = ? AND devuelto = FALSE";
        return contar(sql, idEstudiante, idLibro) > 0;
    }

    @Override
    public boolean update(Prestamo prestamo) throws SQLException {
        String sql = "UPDATE prestamos SET id_estudiante = ?, id_libro = ?, fecha_prestamo = ?, "
                + "fecha_devolucion = ?, devuelto = ? WHERE id = ?";
        Connection conexion = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setInt(1, prestamo.getIdEstudiante());
            statement.setInt(2, prestamo.getIdLibro());
            statement.setDate(3, Date.valueOf(prestamo.getFechaPrestamo()));
            statement.setDate(4, Date.valueOf(prestamo.getFechaVencimiento()));
            statement.setBoolean(5, prestamo.isDevuelto());
            statement.setInt(6, prestamo.getId());
            return statement.executeUpdate() > 0;
        }
    }

    // marca la devolucion; el "AND devuelto = FALSE" evita devolver dos veces el mismo prestamo
    public boolean marcarDevuelto(Connection conexion, int idPrestamo) throws SQLException {
        String sql = "UPDATE prestamos SET devuelto = TRUE WHERE id = ? AND devuelto = FALSE";
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setInt(1, idPrestamo);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return delete(DatabaseConnection.getInstance().getConnection(), id);
    }

    public boolean delete(Connection conexion, int id) throws SQLException {
        String sql = "DELETE FROM prestamos WHERE id = ?";
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private int contar(String sql, int param1, Integer param2) throws SQLException {
        Connection conexion = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setInt(1, param1);
            if (param2 != null) {
                statement.setInt(2, param2);
            }
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private List<Prestamo> consultar(String sql, int... parametros) throws SQLException {
        List<Prestamo> prestamos = new ArrayList<>();
        Connection conexion = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                statement.setInt(i + 1, parametros[i]);
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Date prestado = rs.getDate("fecha_prestamo");
                    Date vence = rs.getDate("fecha_devolucion");
                    Prestamo p = new Prestamo(rs.getInt("id"), rs.getInt("id_estudiante"), rs.getInt("id_libro"),
                            prestado == null ? null : prestado.toLocalDate(),
                            vence == null ? null : vence.toLocalDate(),
                            rs.getBoolean("devuelto"));
                    p.setNombreEstudiante(rs.getString("estudiante"));
                    p.setTituloLibro(rs.getString("libro"));
                    prestamos.add(p);
                }
            }
        }
        return prestamos;
    }
}

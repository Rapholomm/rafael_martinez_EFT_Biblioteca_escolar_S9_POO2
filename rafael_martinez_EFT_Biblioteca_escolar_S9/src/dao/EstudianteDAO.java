package dao;

import modelo.Estudiante;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EstudianteDAO implements CrudDAO<Estudiante> {

    private static final String SELECT_BASE = "SELECT id, nombre, rut, curso, correo FROM estudiantes";

    @Override
    public void create(Estudiante estudiante) throws SQLException {
        String sql = "INSERT INTO estudiantes (nombre, rut, curso, correo) VALUES (?, ?, ?, ?)";
        Connection conexion = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, estudiante.getNombre());
            statement.setString(2, estudiante.getRut());
            statement.setString(3, estudiante.getCurso());
            statement.setString(4, estudiante.getCorreo());
            statement.executeUpdate();
            try (ResultSet claves = statement.getGeneratedKeys()) {
                if (claves.next()) {
                    estudiante.setId(claves.getInt(1));
                }
            }
        }
    }

    @Override
    public List<Estudiante> readAll() throws SQLException {
        List<Estudiante> estudiantes = new ArrayList<>();
        Connection conexion = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = conexion.prepareStatement(SELECT_BASE + " ORDER BY nombre");
             ResultSet resultado = statement.executeQuery()) {
            while (resultado.next()) {
                estudiantes.add(mapear(resultado));
            }
        }
        return estudiantes;
    }

    @Override
    public Estudiante readById(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE id = ?";
        Connection conexion = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    // para vincular un usuario con su ficha de estudiante (mismo rut o mismo correo)
    public Estudiante buscarPorRutOCorreo(String rut, String correo) throws SQLException {
        String sql = SELECT_BASE + " WHERE rut = ? OR correo = ?";
        Connection conexion = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, rut);
            statement.setString(2, correo);
            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    @Override
    public boolean update(Estudiante estudiante) throws SQLException {
        String sql = "UPDATE estudiantes SET nombre = ?, rut = ?, curso = ?, correo = ? WHERE id = ?";
        Connection conexion = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, estudiante.getNombre());
            statement.setString(2, estudiante.getRut());
            statement.setString(3, estudiante.getCurso());
            statement.setString(4, estudiante.getCorreo());
            statement.setInt(5, estudiante.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM estudiantes WHERE id = ?";
        Connection conexion = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private Estudiante mapear(ResultSet rs) throws SQLException {
        return new Estudiante(rs.getInt("id"), rs.getString("nombre"), rs.getString("rut"),
                rs.getString("curso"), rs.getString("correo"));
    }
}
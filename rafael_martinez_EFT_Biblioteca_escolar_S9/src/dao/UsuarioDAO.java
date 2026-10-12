package dao;

import modelo.Rol;
import modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// OJO: la columna de la BD se llama "contraseña" (con ñ), por eso va entre comillas invertidas
public class UsuarioDAO implements CrudDAO<Usuario> {

    private static final String SELECT_BASE = "SELECT id, nombre, rut, correo, `contraseña` AS clave, rol FROM usuarios";

    @Override
    public void create(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, rut, correo, `contraseña`, rol) VALUES (?, ?, ?, ?, ?)";
        Connection conexion = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, usuario.getNombre());
            statement.setString(2, usuario.getRut());
            statement.setString(3, usuario.getCorreo());
            statement.setString(4, usuario.getContrasena());
            statement.setString(5, usuario.getRol().valorBD());
            statement.executeUpdate();
            try (ResultSet claves = statement.getGeneratedKeys()) {
                if (claves.next()) {
                    usuario.setId(claves.getInt(1));
                }
            }
        }
    }

    @Override
    public List<Usuario> readAll() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        Connection conexion = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = conexion.prepareStatement(SELECT_BASE + " ORDER BY id");
             ResultSet resultado = statement.executeQuery()) {
            while (resultado.next()) {
                usuarios.add(mapear(resultado));
            }
        }
        return usuarios;
    }

    @Override
    public Usuario readById(int id) throws SQLException {
        return buscarUno(SELECT_BASE + " WHERE id = ?", String.valueOf(id));
    }

    // se puede ingresar con el correo o con el rut
    public Usuario buscarPorCorreoORut(String identificador) throws SQLException {
        String sql = SELECT_BASE + " WHERE correo = ? OR rut = ?";
        Connection conexion = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, identificador);
            statement.setString(2, identificador);
            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    // usuario asociado a un rut (se usa para enlazar estudiante <-> usuario)
    public Usuario buscarPorRut(String rut) throws SQLException {
        return buscarUno(SELECT_BASE + " WHERE rut = ?", rut);
    }

    @Override
    public boolean update(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuarios SET nombre = ?, rut = ?, correo = ?, `contraseña` = ?, rol = ? WHERE id = ?";
        Connection conexion = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, usuario.getNombre());
            statement.setString(2, usuario.getRut());
            statement.setString(3, usuario.getCorreo());
            statement.setString(4, usuario.getContrasena());
            statement.setString(5, usuario.getRol().valorBD());
            statement.setInt(6, usuario.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        Connection conexion = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private Usuario buscarUno(String sql, String parametro) throws SQLException {
        Connection conexion = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, parametro);
            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(rs.getInt("id"), rs.getString("nombre"), rs.getString("rut"),
                rs.getString("correo"), rs.getString("clave"), Rol.desdeTexto(rs.getString("rol")));
    }
}


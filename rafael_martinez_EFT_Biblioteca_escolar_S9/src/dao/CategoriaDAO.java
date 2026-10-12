package dao;

import modelo.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO implements CrudDAO<Categoria> {

    @Override
    public void create(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO categorias (nombre) VALUES (?)";
        Connection conexion = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, categoria.getNombre());
            statement.executeUpdate();
            try (ResultSet claves = statement.getGeneratedKeys()) {
                if (claves.next()) {
                    categoria.setId(claves.getInt(1));
                }
            }
        }
    }

    @Override
    public List<Categoria> readAll() throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT id, nombre FROM categorias ORDER BY nombre";
        Connection conexion = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = conexion.prepareStatement(sql);
             ResultSet resultado = statement.executeQuery()) {
            while (resultado.next()) {
                categorias.add(new Categoria(resultado.getInt("id"), resultado.getString("nombre")));
            }
        }
        return categorias;
    }

    @Override
    public Categoria readById(int id) throws SQLException {
        String sql = "SELECT id, nombre FROM categorias WHERE id = ?";
        Connection conexion = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return new Categoria(resultado.getInt("id"), resultado.getString("nombre"));
                }
            }
        }
        return null;
    }

    @Override
    public boolean update(Categoria categoria) throws SQLException {
        String sql = "UPDATE categorias SET nombre = ? WHERE id = ?";
        Connection conexion = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, categoria.getNombre());
            statement.setInt(2, categoria.getId());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM categorias WHERE id = ?";
        Connection conexion = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }
}

